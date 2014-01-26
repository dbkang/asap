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
    
    // TODO: all these should come from a separate module definition at some point
    $stateProvider.state('blog', {
      url: '/blog',
      templateUrl: '/static/partials/blog.html',
      controller: ['$scope', function ($scope) {
        $scope.blogTitle = "Dan's Blog"; // TODO: this should come from DB
      }]
    });

    $stateProvider.state('blog.post', {
      url: '/post',
      templateUrl: '/static/partials/blog.post.html',
      controller: 'BlogPost'
    });

    $stateProvider.state('blog.view', {
      url: '/view/:id',
      templateUrl: '/static/partials/blog.view.html'
    });

    $stateProvider.state('blog.edit', {
      url: '/edit/:id',
      templateUrl: '/static/partials/blog.edit.html',
      controller: 'BlogEdit'
    });
  }

}());
