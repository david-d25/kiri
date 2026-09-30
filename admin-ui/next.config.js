/** @type {import('next').NextConfig} */
const nextConfig = {
  // The Docker image ships only the traced server, not the whole project.
  output: 'standalone',
  basePath: '/kiri',
  assetPrefix: '/kiri/',
  productionBrowserSourceMaps: false,
  reactStrictMode: true,
  publicRuntimeConfig: {
    basePath: '/kiri'
  },
  env: {
    NEXT_PUBLIC__API_BASE_URL: process.env.API_BASE_URL,
  },
  turbopack: {
    rules: {
      '*.svg': {
        as: '*.js',
        loaders: ['@svgr/webpack'],
      },
    },
  },
  webpack(config) {
    config.module.rules.push({
      test: /\.svg$/i,
      use: ['@svgr/webpack'],
    });
    return config;
  },
}

module.exports = nextConfig