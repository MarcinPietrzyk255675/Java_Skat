#!/usr/bin/env bash
find . -type f \
  -not -path "*/.idea/*" \
  -not -path "*/.git/*" \
  -not -path "*/target/*" \
  -exec sh -c 'file -b --mime-type "$1" | grep -q "^text/"' _ {} \; \
  -exec echo "=== PLIK: {} ===" \; -exec cat {} \;
