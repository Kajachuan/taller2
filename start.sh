export FLASK_ENV=development
export FLASK_APP=app/app.py
# export CRYPT_KEY="4N8Ylf5VffObUk3JeRe7ha04dOGYc1U5h8eehFrdBAw="
gunicorn --workers 9 --log-level debug --reload app.app:app
