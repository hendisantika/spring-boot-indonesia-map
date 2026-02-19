#!/usr/bin/env python3
import glob
import json
import os
import re
import sys
from collections import defaultdict
from shapely import wkb
from shapely.geometry import MultiPolygon, Polygon

SQL_DIR = "/Users/hendisantika/IdeaProjects/indonesia-map/src/main/resources/db/migration"
OUTPUT_DIR = "/Users/hendisantika/IdeaProjects/spring-boot-indonesia-map/src/main/resources/static/geojson/kecamatan"
COORD_PRECISION = 4

def pcode_to_kode(pcode):
    stripped = pcode[2:]
    prov = stripped[:2]
    kab = stripped[2:4]
    kec = stripped[4:]
    return f"{prov}.{kab}.{kec}"

def pcode_to_parent_kode(adm2_pcode):
    stripped = adm2_pcode[2:]
    prov = stripped[:2]
    kab = stripped[2:4]
    return f"{prov}.{kab}"

def extract_coords_from_geometry(geom):
    polygons = []
    if isinstance(geom, Polygon):
        polygons = [geom]
    elif isinstance(geom, MultiPolygon):
        polygons = list(geom.geoms)
    else:
        if hasattr(geom, 'geoms'):
            for g in geom.geoms:
                if isinstance(g, (Polygon, MultiPolygon)):
                    if isinstance(g, Polygon):
                        polygons.append(g)
                    else:
                        polygons.extend(g.geoms)
    all_rings = []
    for poly in polygons:
        coords = list(poly.exterior.coords)
        ring = [
            [round(lat, COORD_PRECISION), round(lng, COORD_PRECISION)]
            for lng, lat in coords
        ]
        all_rings.append(ring)
    return all_rings

def parse_values_string(rest_values):
    """Parse SQL values string handling MySQL escaped quotes ('' -> ')."""
    values = []
    i = 0
    s = rest_values.strip()
    while i < len(s):
        # Skip whitespace and commas
        while i < len(s) and s[i] in (' ', '\t', '\n', '\r', ','):
            i += 1
        if i >= len(s):
            break

        if s[i] == "'":
            # Quoted string - handle '' escapes
            i += 1  # skip opening quote
            val_chars = []
            while i < len(s):
                if s[i] == "'":
                    # Check if it's an escaped quote ''
                    if i + 1 < len(s) and s[i + 1] == "'":
                        val_chars.append("'")
                        i += 2
                    else:
                        # End of string
                        i += 1
                        break
                else:
                    val_chars.append(s[i])
                    i += 1
            values.append(''.join(val_chars))
        elif s[i:i+4] == 'NULL':
            values.append(None)
            i += 4
        elif s[i] in '-0123456789.':
            # Number
            j = i
            while i < len(s) and s[i] not in (',', ' ', '\t', '\n', '\r', ')'):
                i += 1
            values.append(s[j:i])
        else:
            # Unknown token, skip
            i += 1
    return values

def parse_sql_file(filepath):
    records = []
    filename = os.path.basename(filepath)
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Note: ST_GeomFromWKB(UNHEX('...'), 0) has a second SRID argument
    insert_pattern = re.compile(
        r"ST_GeomFromWKB\(UNHEX\('([0-9A-Fa-f]+)'\),\s*\d+\)\s*,\s*"
        r"(.*?)\);",
        re.DOTALL
    )
    matches = list(insert_pattern.finditer(content))
    print(f"  {filename}: found {len(matches)} INSERT statements")

    for i, match in enumerate(matches):
        hex_str = match.group(1)
        rest_values = match.group(2)

        values = parse_values_string(rest_values)

        if len(values) < 9:
            print(f"    WARNING: Record {i} has only {len(values)} values, skipping")
            continue

        # Order: shape_leng, shape_area, adm3_en, adm3_pcode, adm3_ref,
        #         adm3alt1en, adm3alt2en, adm2_en, adm2_pcode, ...
        adm3_en = values[2]
        adm3_pcode = values[3]
        adm2_pcode = values[8]

        if not adm3_pcode or not adm2_pcode:
            print(f"    WARNING: Record {i} missing pcode, skipping")
            continue

        # Validate pcodes
        if not re.match(r'^ID\d{7}$', adm3_pcode):
            print(f"    WARNING: Record {i} invalid adm3_pcode '{adm3_pcode}', skipping")
            continue
        if not re.match(r'^ID\d{4}$', adm2_pcode):
            print(f"    WARNING: Record {i} invalid adm2_pcode '{adm2_pcode}', skipping")
            continue

        try:
            geom = wkb.loads(bytes.fromhex(hex_str))
        except Exception as e:
            print(f"    WARNING: Record {i} ({adm3_en}) WKB parse error: {e}")
            continue

        centroid = geom.centroid
        lat = round(centroid.y, 6)
        lng = round(centroid.x, 6)
        path = extract_coords_from_geometry(geom)
        kode = pcode_to_kode(adm3_pcode)
        parent_kode = pcode_to_parent_kode(adm2_pcode)

        records.append({
            "kode": kode,
            "nama": adm3_en,
            "lat": lat,
            "lng": lng,
            "path": path,
            "parent_kode": parent_kode,
        })

    return records

sql_files = []
for i in range(1, 8):
    pattern = os.path.join(SQL_DIR, f"V9.{i}*.sql")
    sql_files.extend(sorted(glob.glob(pattern)))

if not sql_files:
    print("ERROR: No SQL files found!")
    sys.exit(1)

print(f"Found {len(sql_files)} SQL files:")
for f in sql_files:
    print(f"  {os.path.basename(f)}")
print()

all_records = []
errors = 0
for filepath in sql_files:
    records = parse_sql_file(filepath)
    all_records.extend(records)

print(f"\nTotal kecamatan extracted: {len(all_records)}")

by_kabupaten = defaultdict(list)
for rec in all_records:
    parent_kode = rec.pop("parent_kode")
    by_kabupaten[parent_kode].append(rec)

print(f"Total kabupaten (parent_kode groups): {len(by_kabupaten)}")

os.makedirs(OUTPUT_DIR, exist_ok=True)

# Clean up any previous anomalous files
for fn in os.listdir(OUTPUT_DIR):
    if fn.endswith('.json'):
        os.remove(os.path.join(OUTPUT_DIR, fn))

total_files = 0
total_size = 0
file_sizes = []

for parent_kode in sorted(by_kabupaten.keys()):
    kecamatan_list = by_kabupaten[parent_kode]
    kecamatan_list.sort(key=lambda x: x["kode"])
    output_path = os.path.join(OUTPUT_DIR, f"{parent_kode}.json")
    json_str = json.dumps(kecamatan_list, separators=(',', ':'), ensure_ascii=False)
    with open(output_path, 'w', encoding='utf-8') as f:
        f.write(json_str)
    fsize = os.path.getsize(output_path)
    total_size += fsize
    file_sizes.append((parent_kode, fsize, len(kecamatan_list)))
    total_files += 1

print(f"Total JSON files created: {total_files}")
print(f"Total output size: {total_size / 1024 / 1024:.2f} MB")
print()

file_sizes.sort(key=lambda x: x[1], reverse=True)
print("Top 10 largest files:")
for kode, size, count in file_sizes[:10]:
    print(f"  {kode}.json: {size / 1024:.1f} KB ({count} kecamatan)")

print("\nTop 10 smallest files:")
for kode, size, count in file_sizes[-10:]:
    print(f"  {kode}.json: {size / 1024:.1f} KB ({count} kecamatan)")

sizes_only = [s for _, s, _ in file_sizes]
avg_size = sum(sizes_only) / len(sizes_only) if sizes_only else 0
print(f"\nAverage file size: {avg_size / 1024:.1f} KB")
if sizes_only:
    print(f"Median file size: {sorted(sizes_only)[len(sizes_only) // 2] / 1024:.1f} KB")

print("\nKecamatan count by region:")
region_counts = defaultdict(int)
for parent_kode, kecamatan_list in by_kabupaten.items():
    prov = parent_kode.split('.')[0]
    region_counts[prov] += len(kecamatan_list)
for prov in sorted(region_counts.keys()):
    print(f"  Province {prov}: {region_counts[prov]} kecamatan")

# Verify no anomalous files
anomalous = []
for fn in sorted(os.listdir(OUTPUT_DIR)):
    if not fn.endswith('.json'):
        continue
    name = fn[:-5]
    parts = name.split('.')
    if len(parts) != 2 or not parts[0].isdigit() or not parts[1].isdigit():
        anomalous.append(fn)

if anomalous:
    print(f"\nWARNING: {len(anomalous)} anomalous files found:")
    for fn in anomalous:
        print(f"  {fn}")
else:
    print("\nNo anomalous files - all filenames match XX.XX.json pattern")

if by_kabupaten:
    sample_kode = sorted(by_kabupaten.keys())[0]
    sample_path = os.path.join(OUTPUT_DIR, f"{sample_kode}.json")
    with open(sample_path, 'r') as f:
        sample_data = json.load(f)
    print(f"\nSample verification - {sample_kode}.json:")
    print(f"  Contains {len(sample_data)} kecamatan")
    if sample_data:
        first = sample_data[0]
        print(f"  First entry: kode={first['kode']}, nama={first['nama']}, lat={first['lat']}, lng={first['lng']}")
        print(f"  Path rings: {len(first['path'])}, first ring points: {len(first['path'][0]) if first['path'] else 0}")
        if first['path'] and first['path'][0]:
            print(f"  First 3 coords: {first['path'][0][:3]}")

    # Also verify one of the previously problematic records
    for pk in by_kabupaten:
        for rec in by_kabupaten[pk]:
            if "'" in rec.get('nama', ''):
                print(f"\n  Apostrophe name verified: kode={rec['kode']}, nama={rec['nama']}")
                break

print("\nDone!")
