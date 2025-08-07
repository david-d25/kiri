ARG API_BASE_URL

FROM node:20-alpine AS builder
ARG API_BASE_URL
ENV API_BASE_URL=${API_BASE_URL}
WORKDIR /app
COPY . .
RUN npm ci
RUN npm run build

FROM node:20-alpine AS runner
WORKDIR /app
ARG API_BASE_URL
ENV API_BASE_URL=$API_BASE_URL
COPY --from=builder /app .
EXPOSE 3000
CMD ["npm", "run", "start"]