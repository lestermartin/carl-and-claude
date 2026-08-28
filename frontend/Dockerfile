# ---- build stage ----
FROM node:22-alpine AS build
WORKDIR /app
ENV CI=true
ENV NG_CLI_ANALYTICS=false

COPY package.json ./
RUN npm install

COPY . .
RUN npm run build

# ---- runtime stage ----
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/trading-app/browser /usr/share/nginx/html
EXPOSE 80
