import logging
from logging.handlers import SMTPHandler
from os import makedirs, path, environ
from flask import Flask, redirect, url_for
from flask_mongoengine import MongoEngine

app = Flask(__name__)
app.config.from_pyfile('../config/app.py')
db = MongoEngine(app)

from .controllers.users import users
from .controllers.sessions import sessions
from .controllers.organizations import organizations
from .controllers.admins import admins
from .controllers.passwords import passwords
from .controllers.statistics import statistics
from .models.firebase_api import FirebaseApi

app.register_blueprint(users)
app.register_blueprint(sessions)
app.register_blueprint(organizations)
app.register_blueprint(admins)
app.register_blueprint(passwords)
app.register_blueprint(statistics)

@app.route('/')
def index():
    return redirect(url_for('admins.admin_login'))

FirebaseApi.start_service()

if __name__ != '__main__':
    makedirs(path.dirname('logs/app.log'), exist_ok=True)

    file_handler = logging.FileHandler('logs/app.log')
    formatter = logging.Formatter(fmt='[%(asctime)s] [%(levelname)s] [%(process)s] %(message)s',
                                  datefmt='%Y-%m-%d %H:%M:%S %z')
    file_handler.setFormatter(formatter)

    gunicorn_logger = logging.getLogger('gunicorn.error')
    gunicorn_logger.addHandler(file_handler)
    if not app.debug:
        mail_handler = SMTPHandler(mailhost=(environ['SENDGRID_ADDRESS'], environ['SENDGRID_PORT']),
                                   fromaddr='hypechat@error.com',
                                   toaddrs=['kevincajachuan@gmail.com',
                                            'guillecondori19@gmail.com',
                                            'Fabrizio.Cozza@gmail.com'],
                                   subject='Application Error',
                                   credentials=(environ['SENDGRID_USERNAME'],environ['SENDGRID_PASSWORD']))
        mail_handler.setLevel(logging.ERROR)
        mail_handler.setFormatter(logging.Formatter('[%(asctime)s] %(levelname)s in %(module)s: %(message)s'))
        gunicorn_logger.addHandler(mail_handler)

    app.logger.handlers = gunicorn_logger.handlers
    app.logger.setLevel(gunicorn_logger.level)
