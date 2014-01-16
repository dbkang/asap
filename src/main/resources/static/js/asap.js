'use strict';

(function () {
  var appModule = angular.module('asap', ['ui.router']);
  appModule.config(['$stateProvider', '$urlRouterProvider', '$locationProvider', routeConfig]);
  function routeConfig($stateProvider, $urlRouterProvider, $locationProvider) {
    $locationProvider.html5Mode(true).hashPrefix('!');
    $urlRouterProvider.otherwise('/home');
    $stateProvider.state('home', {
      url: '/home',
      template: '<h1>HOME!</h1>'
    });
  }

}());
