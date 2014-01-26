'use strict';

(function () {
  angular.module('asap').controller('BlogPost', ['$scope', '$state', '$http', BlogPost]);
  function BlogPost($scope, $state, $http) {
    $scope.post = {};
    $scope.post.title = "Type title here";
    $scope.post.body = "Type body here";


    $scope.savePost = function(post) {
      $http.post('/api/autostore/default/blog', post).then(function(response) {
        $state.transitionTo('blog.view', { id: response.data });
      })
    };

    $scope.cancel = function () {
      $state.transitionTo('blog');
    };
  }
}());
