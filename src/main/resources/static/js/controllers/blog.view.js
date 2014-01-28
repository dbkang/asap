'use strict';

(function () {
  angular.module('asap').controller('BlogView', ['$scope', '$state', '$stateParams', '$http', BlogView]);
  function BlogView($scope, $state, $stateParams, $http) {
    $scope.post = {};

    $http.get('/api/autostore/default/blog/' + $stateParams.id).then(function(response) {
      $scope.post = response.data;
    });
  }
}());
