import logging
from os import makedirs, path
from flask import Flask, redirect, url_for
from flask_mongoengine import MongoEngine

app = Flask(__name__)
app.config.from_pyfile('../config/app.py')
db = MongoEngine(app)

from .controllers.users import users
from .controllers.sessions import sessions
from .controllers.admins import admins
from .controllers.passwords import passwords

app.register_blueprint(users)
app.register_blueprint(sessions)
app.register_blueprint(admins)
app.register_blueprint(passwords)

@app.route('/')
def index():
    return redirect(url_for('admins.admin_login'))

if __name__ != '__main__':
    makedirs(path.dirname('logs/app.log'), exist_ok=True)

    file_handler = logging.FileHandler('logs/app.log')
    formatter = logging.Formatter(fmt='[%(asctime)s] [%(levelname)s] [%(process)s] %(message)s',
                                  datefmt='%Y-%m-%d %H:%M:%S %z')
    file_handler.setFormatter(formatter)
    gunicorn_logger = logging.getLogger('gunicorn.error')
    gunicorn_logger.addHandler(file_handler)

    app.logger.handlers = gunicorn_logger.handlers
    app.logger.setLevel(gunicorn_logger.level)
