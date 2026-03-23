FROM node:22.21.1-alpine AS base
WORKDIR /usr/src/wpp-server
ENV PUPPETEER_SKIP_CHROMIUM_DOWNLOAD=true



# Install build dependencies and runtime libraries for sharp
RUN apk update && \
    apk add --no-cache \
    vips \
    vips-dev \
    fftw-dev \
    gcc \
    g++ \
    make \
    libc6-compat \
    pkgconfig \
    python3 \
    && rm -rf /var/cache/apk/*

# ... (seu código anterior do Dockerfile fica aqui em cima) ...

# Copia o nosso script de limpeza para dentro do container
COPY start.sh /usr/src/wpp-server/start.sh

# Dá permissão para o Linux executar o arquivo
RUN chmod +x /usr/src/wpp-server/start.sh

# Define o script como o portão de entrada do container
ENTRYPOINT ["/usr/src/wpp-server/start.sh"]

# O comando final que liga o servidor (mantenha o que você já usava, geralmente é yarn start)
CMD ["yarn", "start"]

# To make sure yarn 4 uses node-modules linker
COPY .yarnrc.yml ./

# Copy only package.json to leverage Docker cache
COPY package.json ./
COPY yarn.lock ./

# Enable corepack and prepare yarn 4.12.0
RUN corepack enable && \
    corepack prepare yarn@4.12.0 --activate

# Install dependencies with immutable lockfile
RUN yarn install --immutable

FROM base AS build
WORKDIR /usr/src/wpp-server
COPY . .
RUN yarn install
RUN yarn build

FROM build AS runtime
WORKDIR /usr/src/wpp-server/

# Install runtime dependencies (chromium and vips libraries)
RUN apk add --no-cache \
    chromium \
    vips \
    fftw

EXPOSE 21465
ENTRYPOINT ["node", "dist/server.js"]
