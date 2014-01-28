'use strict';

(function () {
  angular.module('asap').controller('BlogEdit', ['$scope', '$state', '$stateParams', BlogEdit]);
  function BlogEdit($scope, $state, $stateParams) {
    $scope.post = {};
    $scope.post.title = "New Title";
    $scope.post.body = "Type body here";

    $http.get('/api/autostore/default/blog/' + $stateParms.id).then(function(response) {
      $scope.post = response.data;
    });

    $scope.savePost = function(post) {
      $http.put('/api/autostore/default/blog/' + $stateParams.id, post).then(function(response) {
        $state.transitionTo('blog.view', { id: response.data });
      });
    };

    $scope.cancel = function () {
      $state.transitionTo('blog.view', { id: $stateParams.id });
    };
  }
}());
