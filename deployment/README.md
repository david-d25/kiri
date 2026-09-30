# Deployment

Kiri runs on the VDS as three containers — an nginx gateway, the backend and
the admin UI — behind the host's own nginx, which terminates TLS and proxies
the `/kiri` subtree into the port published here. PostgreSQL is not part of
this project; the backend reaches it on the host through `host.docker.internal`.

Nothing is built on the server: CI builds both images, tags them with the
commit, and pushes them to GHCR — the server only pulls. The workflows are
in `.github/workflows`:

- **Deploy** — every push to `main`, or by hand. Builds, tests, publishes and
  restarts.
- **Undeploy** — by hand only. Stops and removes the containers; `.env` and
  the config stay on the server, so running Deploy again brings everything back.

## One-time setup on the server

```sh
# A user that can talk to docker and owns nothing else.
sudo adduser --disabled-password --gecos "" deploy
sudo usermod -aG docker deploy

sudo install -d -o deploy -g deploy /srv/kiri
```

Add the CI public key to `/home/deploy/.ssh/authorized_keys`. Membership of
the `docker` group is equivalent to root, so treat that key accordingly.

Then, in `/srv/kiri`:

- copy `.env.example` to `.env` and fill it in, with `APP_UID` and `APP_GID`
  set to the output of `id -u deploy` and `id -g deploy` — the backend runs
  as that user;
- copy `application.example.yml` to `application.yml` (or point `CONFIG_PATH`
  at an existing one) and fill it in. It holds the encryption key and the bot
  token, so keep it readable by `deploy` alone:
  `chown deploy:deploy application.yml && chmod 600 application.yml`.
  That exposes nothing new: `deploy` is in the `docker` group and could read
  any file on the host anyway.

Neither file is in git and CI never overwrites them. On the first deploy the
examples are shipped along with the rest; until then, take them from the repo.

PostgreSQL has to accept connections from the Docker bridge network, not only
from `localhost`: check `listen_addresses` and `pg_hba.conf`.

## The host's nginx

One rule for the whole application. The prefix is *not* stripped: the
backend expects `/kiri/api` as its context path and Next expects `/kiri` as
its base path.

```nginx
location /kiri/ {
    proxy_pass         http://127.0.0.1:8080;
    proxy_http_version 1.1;
    proxy_set_header   Host              $host;
    proxy_set_header   X-Real-IP         $remote_addr;
    proxy_set_header   X-Forwarded-For   $proxy_add_x_forwarded_for;
    proxy_set_header   X-Forwarded-Proto $scheme;
    proxy_buffering    off;   # server-sent events
}
```

The auth cookies are `Secure`, so this has to be served over HTTPS.

## GitHub

Repository **variables** (Settings → Secrets and variables → Actions):

| | |
|---|---|
| `DEPLOY_HOST` | the VDS |
| `DEPLOY_USER` | `deploy` |
| `DEPLOY_PATH` | `/srv/kiri` |
| `DEPLOY_HOST_KEY` | optional: the host's line from `ssh-keyscan -t ed25519 <host>`, to pin it |

Repository **secret**: `DEPLOY_KEY` — the private half of the key above.

Until `DEPLOY_HOST` is set the deploy job is skipped, so building and
publishing work from the first commit.

The workflow signs in to GHCR with the token Actions already has, but the
first push **creates both packages as private**, and the server has to be
allowed to pull them. Either make `kiri/backend` and `kiri/admin-ui` public
(Package settings → Change visibility; neither contains secrets, the config
is mounted at run time), or sign in once on the server with a token that has
`read:packages`:

```sh
echo <token> | docker login ghcr.io -u <user> --password-stdin
```

This is the usual reason a first deploy fails with `denied` or
`manifest unknown`.

## By hand, on the server

```sh
cd /srv/kiri
docker compose pull && docker compose up -d     # deploy whatever TAG says
docker compose down                             # undeploy
docker compose logs -f backend
```

Rolling back is the same with an older commit (or a version — every image is
also tagged with the `version` from `build.gradle.kts`):

```sh
sed -i 's/^TAG=.*/TAG=<sha>/' .env && docker compose up -d
```

Flyway migrations run on startup and are not undone by a rollback: going back
past a migration means the old code runs against the new schema.
