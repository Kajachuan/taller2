from http import HTTPStatus
from flask import Blueprint, request, session, current_app, render_template, redirect, flash, jsonify
from ..models.admin import Admin
from ..models.forbidden_words import ForbiddenWords

admins = Blueprint('admins', __name__)

@admins.route('/admin/', methods=['GET', 'POST'])
def admin_login():
    if request.method == 'GET':
        return render_template('login.html')

    name = request.form['name']
    current_app.logger.debug('The name is: ' + name)
    password = request.form['password']

    admin = Admin.authenticate(name, password)
    if not admin:
        current_app.logger.info('The name or password are wrong')
        flash('El nombre y/o la contraseña son incorrectos')
        return redirect('/admin/')

    session['admin'] = admin.name
    current_app.logger.info('The admin ' + name + ' is logged in')
    return redirect('/admin/home/')

@admins.route('/admin/logout/', methods=['POST'])
def admin_logout():
    name = session.pop('admin')
    current_app.logger.info('The admin ' + name + ' was logged out')
    return redirect('/admin/')

@admins.route('/admin/menu/', methods=['GET'])
def menu():
    current_app.logger.info('Viewing menu')
    return render_template('menu.html')

@admins.route('/admin/forbidden-words/', methods =['GET'])
def get_forbidden_words():
    current_app.logger.info('Viewing forbidden words')
    return render_template('forbidden_words.html')

@admins.route('/admin/forbidden-words/words', methods = ['GET'])
def get_forbidden_words_get():
    return jsonify(list_of_words = ForbiddenWords.get_words()), HTTPStatus.OK

@admins.route('/admin/forbidden-words/words', methods = ['POST'])
def add_forbidden_word():
    word = request.form['word']
    ForbiddenWords.add_word(word)
    return redirect('/admin/forbidden-words/')

@admins.route('/admin/forbidden-words/word-delete', methods=['POST'])
def delete_forbidden_word():
    word = request.form['word']
    ForbiddenWords.delete_word(word)
    return redirect('/admin/forbidden-words/')

@admins.route('/admin/home/', methods=['GET'])
def home():
    return render_template('home.html')

@admins.route('/admin/statistics/', methods=['GET'])
def statistics():
    return render_template('statistics.html')
