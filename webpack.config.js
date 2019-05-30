const webpack = require('webpack');
const resolve = require('path').resolve;

const config = {
  entry: {
    login: './app/static/scripts/login.jsx',
    home: './app/static/scripts/home.jsx',
    statistics: './app/static/scripts/statistics.jsx',
    users: './app/static/scripts/users.jsx',
    organizations: './app/static/scripts/organizations.jsx'
  },
  output: {
    path: resolve('app/static/js'),
    filename: '[name].js',
    publicPath: resolve('app/static/js')
  },
  resolve: {
    extensions: ['.js','.jsx','.css']
  },
  module: {
    rules: [{
      test: /\.jsx?/,
      loader: 'babel-loader',
      exclude: /node_modules/,
    }]
  }
};

module.exports = config;
