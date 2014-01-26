'use strict';

(function () {
  angular.module('asap').controller('BlogEdit', ['$scope', '$state', '$stateParams', BlogEdit]);
  function BlogEdit($scope, $state, $stateParams) {
    $scope.post = {};
    $scope.post.title = "New Title";
    $scope.post.body = "Type body here";


    $scope.savePost = function(post) {
      // TODO: Save
      $state.transitionTo('blog.view', { id: $stateParams.id });
    };

    $scope.cancel = function () {
      $state.transitionTo('blog.view', { id: $stateParams.id });
    };
  }
}());
