from flask import Flask
from flask_mongoengine import MongoEngine
from os import environ

app = Flask(__name__)
app.config.from_object('taller2.config.app.' + environ['FLASK_ENV'].capitalize() + 'Config')
db = MongoEngine(app)

from .controllers.users import users

app.register_blueprint(users)

@app.route('/')
def root():
    return 'Hello, World!'
