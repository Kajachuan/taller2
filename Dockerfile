FROM python:3.7-slim

WORKDIR .

RUN pip3 install -r requirements.txt

EXPOSE 8000

ENV FLASK_ENV="development" FLASK_APP="app/app.py" CRYPT_KEY="4N8Ylf5VffObUk3JeRe7ha04dOGYc1U5h8eehFrdBAw="

CMD gunicorn --workers 9 app.app:app
