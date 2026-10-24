#!/usr/bin/env bash
# ==============================================================================
# GitHub Raw metadata.json Updater & Version Checker Script
# Usage:
#   ./check_github_update.sh <GITHUB_RAW_URL> [CURRENT_VERSION] [OUTPUT_DIR]
# Example:
#   ./check_github_update.sh https://raw.githubusercontent.com/example/repo/main/metadata.json 3.0 ./downloads
# ==============================================================================

set -euo pipefail

RAW_URL="${1:-}"
CURRENT_VERSION="${2:-1.0.0}"
OUTPUT_DIR="${3:-./updates}"

if [ -z "$RAW_URL" ]; then
  echo "Hata: Lütfen bir GitHub raw metadata.json URL'si belirtin!"
  echo "Kullanım: $0 <RAW_URL> [CURRENT_VERSION] [OUTPUT_DIR]"
  echo "Örnek:    $0 https://raw.githubusercontent.com/user/app/main/metadata.json 2.0 ./updates"
  exit 1
fi

# Ensure URL is raw
RAW_URL="${RAW_URL/github.com/raw.githubusercontent.com}"
RAW_URL="${RAW_URL/\/blob\///}"

echo "=================================================="
echo "🔍 GitHub Raw Metadata.json Denetleniyor..."
echo "🔗 URL:             $RAW_URL"
echo "📱 Mevcut Sürüm:   $CURRENT_VERSION"
echo "📁 İndirme Dizini: $OUTPUT_DIR"
echo "=================================================="

# Fetch metadata.json via curl
TEMP_META=$(mktemp)
HTTP_CODE=$(curl -sSL -w "%{http_code}" -o "$TEMP_META" "$RAW_URL" || echo "000")

if [ "$HTTP_CODE" -ne 200 ]; then
  echo "❌ Hata: metadata.json dosyası çekilemedi! HTTP Kodu: $HTTP_CODE"
  rm -f "$TEMP_META"
  exit 2
fi

echo "✅ metadata.json başarıyla indirildi."

# Parse fields using python3 or jq
python3 -c "
import sys, json, os, urllib.request, hashlib

meta_file = '$TEMP_META'
curr_version = '$CURRENT_VERSION'
out_dir = '$OUTPUT_DIR'

with open(meta_file, 'r', encoding='utf-8') as f:
    data = json.load(f)

remote_name = data.get('name') or data.get('appName', 'App')
remote_version = data.get('version') or data.get('versionName') or data.get('tag_name', '1.0.0')
remote_vcode = data.get('versionCode') or data.get('buildNumber', 1)
download_url = data.get('downloadUrl') or data.get('download_url') or data.get('url') or data.get('apk_url', '')
file_name = data.get('fileName') or data.get('file_name', f'update_{remote_version}.apk')
sha256_expected = data.get('sha256') or data.get('sha256_checksum', '')
changelog = data.get('changelog', [])

print(f'📦 Uzak Paket:    {remote_name}')
print(f'🚀 Uzak Sürüm:    {remote_version} (Kod: {remote_vcode})')
print(f'🔗 İndirme URL:   {download_url}')
if changelog:
    print('📝 Değişiklikler:')
    if isinstance(changelog, list):
        for item in changelog:
            print(f'   • {item}')
    else:
        print(f'   {changelog}')

def clean_ver(v):
    import re
    nums = re.findall(r'\d+', str(v))
    return [int(x) for x in nums] if nums else [0]

is_newer = clean_ver(remote_version) > clean_ver(curr_version)

if not is_newer:
    print('✨ Sisteminiz veya uygulamanız zaten en son güncel sürümde!')
    sys.exit(0)

print(f'\n🔥 YENİ SÜRÜM BULUNDU! ({curr_version} -> {remote_version})')

if not download_url:
    print('⚠️ Uyarı: metadata.json dosyasında indirme linki (downloadUrl) bulunamadı.')
    sys.exit(0)

os.makedirs(out_dir, exist_ok=True)
dest_path = os.path.join(out_dir, file_name)

print(f'⏳ Güncelleme indiriliyor -> {dest_path}...')

def reporthook(count, block_size, total_size):
    if total_size > 0:
        percent = int(count * block_size * 100 / total_size)
        mb = (count * block_size) / (1024 * 1024)
        total_mb = total_size / (1024 * 1024)
        sys.stdout.write(f'\r   İlerleme: %{min(100, percent)} ({mb:.2f}MB / {total_mb:.2f}MB)')
        sys.stdout.flush()

urllib.request.urlretrieve(download_url, dest_path, reporthook)
print('\n✅ İndirme tamamlandı!')

if sha256_expected:
    hasher = hashlib.sha256()
    with open(dest_path, 'rb') as f:
        while chunk := f.read(8192):
            hasher.update(chunk)
    calc_hash = hasher.hexdigest()
    if calc_hash.lower() == sha256_expected.lower():
        print(f'🔒 SHA-256 Doğrulandı: {calc_hash}')
    else:
        print(f'❌ SHA-256 Hatası! Beklenen: {sha256_expected}, Hesaplanan: {calc_hash}')
"

rm -f "$TEMP_META"
echo "🎉 İşlem başarıyla tamamlandı."
