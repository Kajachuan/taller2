from flask import Flask
from flask_mongoengine import MongoEngine
from os import environ

app = Flask(__name__)
app.config['MONGODB_SETTINGS'] = {
    'db': 'hypechat',
    'host': environ['MONGODB_URI']
}
db = MongoEngine(app)

@app.route('/')
def hello_world():
    from .models.user import User
    
    u = User(name='Gino')
    u.save()
    return 'Hello, World!'
