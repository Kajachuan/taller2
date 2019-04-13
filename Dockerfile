FROM python:3.7-slim

WORKDIR /app

COPY . /app

RUN pip3 install -r requirements.txt

EXPOSE 8000

ENV FLASK_ENV=development

CMD gunicorn --chdir app app:app
