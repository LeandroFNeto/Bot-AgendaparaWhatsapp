#!/bin/sh

echo "🧹 Limpando possíveis arquivos de lock do Chromium..."
# Remove os arquivos que travam o WPPConnect se o container for reiniciado bruscamente
rm -f /usr/src/wpp-server/tokens/*/Default/SingletonLock

echo "🚀 Iniciando o WPPConnect Server..."
npm run start