const webpack = require('webpack');
const resolve = require('path').resolve;

const config = {
  entry: {
    login: './app/static/scripts/login.jsx',
    menu: './app/static/scripts/menu.jsx',
    forbidden_words: './app/static/scripts/forbidden_words.jsx'
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
