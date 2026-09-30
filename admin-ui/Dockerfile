# syntax=docker/dockerfile:1

# The admin UI, as a runnable image.
#
# Three stages so that the npm cache, the build and the runtime each have
# their own layer: editing a component re-runs only the build, and touching
# `package-lock.json` is the only thing that re-installs anything.
#
# The runtime stage carries Next's standalone output — the app plus exactly
# the dependencies it turned out to need — instead of the whole project with
# its dev dependencies.
#
# The base path (/kiri) is fixed in next.config.js, so there is nothing to
# pass in at build time.

FROM node:20-alpine AS deps
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci


FROM node:20-alpine AS builder
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .
ENV NEXT_TELEMETRY_DISABLED=1
RUN npm run build


FROM node:20-alpine AS runner
WORKDIR /app

ENV NODE_ENV=production \
    NEXT_TELEMETRY_DISABLED=1 \
    PORT=3000 \
    HOSTNAME=0.0.0.0

# `public` and `.next/static` are served straight off disk and are not part of
# the traced output, so they are copied separately.
COPY --from=builder --chown=node:node /app/public ./public
COPY --from=builder --chown=node:node /app/.next/standalone ./
COPY --from=builder --chown=node:node /app/.next/static ./.next/static

USER node
EXPOSE 3000

CMD ["node", "server.js"]
