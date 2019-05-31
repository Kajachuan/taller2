from ..app import db

class ForbiddenWords(db.Document):
    list_of_words = db.ListField(db.StringField())
    meta = {'strict': False}

    @classmethod
    def get_words(cls):
        forbidden_words = cls.objects[0]
        return forbidden_words.list_of_words

    @classmethod
    def add_word(cls, a_word):
        forbidden_words = cls.objects[0]
        forbidden_words.update(push__list_of_words = a_word)

    @classmethod
    def delete_word(cls, a_word):
        forbidden_words = cls.objects[0]
        forbidden_words.update(pull__list_of_words = a_word)
