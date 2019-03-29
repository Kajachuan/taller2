from ..app import *

class User(db.Document):
    name = db.StringField(max_length=200, required=True)
