# Docker Production Deploy

This setup runs the full app with Docker Compose:

- `db` (PostgreSQL)
- `backend` (Spring Boot, `prod` profile)
- `web` (Nginx serving React build + reverse proxy to backend)

## 1) Prerequisites on server

- Docker Engine + Docker Compose plugin installed
- Ports `80` and `443` open
- DNS `app.example.com` pointing to this server

## 2) Prepare env file

From `erp-saas-backend/deploy`:

```bash
cp .env.docker.example .env.docker
```

Edit `deploy/.env.docker`:

- `JWT_SECRET` (long random)
- `FRONTEND_BASE_URL`
- `CORS_ALLOWED_ORIGINS`
- DB credentials if changed

## 3) Build and start

Run from `erp-saas-backend/deploy`:

```bash
docker compose --env-file .env.docker -f docker-compose.prod.yml up -d --build
```

## 4) Verify

```bash
docker compose --env-file .env.docker -f docker-compose.prod.yml ps
docker compose --env-file .env.docker -f docker-compose.prod.yml logs -f backend
```

Open `http://<server-ip>/` (or your domain once DNS is live).

## 5) Backup

```bash
chmod +x scripts/backup-postgres.sh
./scripts/backup-postgres.sh
```

Backups are saved under `deploy/backups/`.

## 6) HTTPS

Recommended: put Cloudflare or a host-level reverse proxy with TLS in front.
If you terminate TLS directly on server, use certbot and expose container port on 80/443 via a host Nginx.
