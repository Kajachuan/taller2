from flask import Flask
from flask_mongoengine import MongoEngine
from os import environ

app = Flask(__name__)
app.config.from_pyfile('../config/app.py')
db = MongoEngine(app)

try:
    from .controllers.users import users
except ImportError:
    from controllers.users import users

app.register_blueprint(users)

@app.route('/')
def root():
    return 'Hello, World!'
