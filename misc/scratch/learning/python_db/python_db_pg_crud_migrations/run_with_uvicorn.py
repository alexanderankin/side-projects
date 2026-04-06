import uvicorn

from python_db_pg_crud_migrations.api import app

if __name__ == "__main__":
    uvicorn.run(
        app,
        host="127.0.0.1",
        port=8000,
        log_level="debug",
        reload=False,  # IMPORTANT: disable for debugger
        workers=1  # IMPORTANT: single process
    )
