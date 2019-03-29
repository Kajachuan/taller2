from flask import Flask
from flask_mongoengine import MongoEngine
from os import environ

app = Flask(__name__)
app.config.from_object('taller2.config.app.' + environ['FLASK_ENV'].capitalize() + 'Config')
db = MongoEngine(app)

@app.route('/')
def hello_world():
    from .models.user import User

    u = User(name='Gino')
    u.save()
    return 'Hello, World!'
