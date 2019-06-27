export FLASK_ENV=development
export FLASK_APP=app/app.py
export CRYPT_KEY="4N8Ylf5VffObUk3JeRe7ha04dOGYc1U5h8eehFrdBAw="
export MONGODB_URI="mongodb://localhost:27017/hypechat"
gunicorn --log-level debug --workers 2 --reload app.app:app
