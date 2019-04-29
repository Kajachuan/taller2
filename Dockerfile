FROM python:3.6

WORKDIR /app

COPY . /app

RUN pip3 install -r requirements.txt

EXPOSE 8000

ENV FLASK_ENV="development" FLASK_APP="app/app.py" \
    CRYPT_KEY="4N8Ylf5VffObUk3JeRe7ha04dOGYc1U5h8eehFrdBAw=" MONGODB_URI="mongodb://mongo:27017/hypechat"

CMD gunicorn --bind 0.0.0.0:8000 --workers 9 app.app:app
