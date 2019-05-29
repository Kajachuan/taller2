from http import HTTPStatus
from flask import Blueprint, request, session, current_app, render_template, redirect, flash
from ..models.admin import Admin
from ..decorators.admin_required import admin_required

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
@admin_required
def admin_logout():
    name = session.pop('admin')
    current_app.logger.info('The admin ' + name + ' was logged out')
    return redirect('/admin/')

@admins.route('/admin/home/', methods=['GET'])
@admin_required
def home():
    return render_template('home.html')

@admins.route('/admin/statistics/', methods=['GET'])
@admin_required
def statistics():
    return render_template('statistics.html')

@admins.route('/admin/users/', methods=['GET'])
@admin_required
def users_admin():
    return render_template('users.html')
