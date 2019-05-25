const webpack = require('webpack');
const resolve = require('path').resolve;

const config = {
  entry: {
    login: './app/static/scripts/login.jsx',
    forbidden_words: './app/static/scripts/forbidden_words.jsx',
    home: './app/static/scripts/home.jsx',
    statistics: './app/static/scripts/statistics.jsx'
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
