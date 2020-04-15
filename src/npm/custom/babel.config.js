module.exports = {
  presets: [
    [
      '@vue/app',
      {
        "modules": false,
        "targets": {
          "browsers": ["> 1%", "last 2 versions", "not ie <= 8", "ie >= 11"]
        }
      }
    ]
  ],
  "plugins": [
    "@babel/plugin-syntax-dynamic-import",
    "@babel/plugin-transform-runtime",
  ]
};
