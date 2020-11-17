angular.module('todo-app', [ 'toaster' ]);

function TaskCtrl($scope, $http, $timeout, toaster) {
	$scope.tasks = [];
	$scope.completedTasks = [];

	var socket = new SockJS('/tasks');
	stompClient = Stomp.over(socket);
	stompClient.connect({}, function(frame) {
		stompClient.subscribe('/user/queue/task-updates', function(event) {
			var taskEvent = JSON.parse(event.body);
			toaster.pop('success', taskEvent.type, taskEvent.data);
			if (taskEvent.type == "TaskEventCreated") {
				var task = { 'id': taskEvent.data.id, 'title': taskEvent.data.title, 'completed': false, 'starred': false };
				$scope.tasks.unshift(task);
			} else if (taskEvent.type == "TaskEventCompleted") {
				var key = getTaskKey($scope.tasks, taskEvent.data.id);
				var task = $scope.tasks[key];
				task.completed = true;
				$scope.completedTasks.unshift(task);
				$scope.tasks.splice(key, 1);
			} else if (taskEvent.type == "TaskEventStarred") {
				var key = getTaskKey($scope.tasks, taskEvent.data.id);
				$scope.tasks[key].starred = true;
			} else if (taskEvent.type == "TaskEventUnstarred") {
				var key = getTaskKey($scope.tasks, taskEvent.data.id);
				$scope.tasks[key].starred = false;
			} else {
				// modify data for other events (such as modify title, etc)
				var key = getTaskKey($scope.tasks, taskEvent.data.id);
				angular.extend($scope.tasks[key], taskEvent.data);
			}

			$scope.$apply();
		});
		stompClient.subscribe('/user/queue/errors', function(event) {
			toaster.pop('error', "Error", event.body);
			$scope.$apply();
		});
	});

	$scope.loadTasks = function() {
		$scope.taskDetails = null;
		$http.get('api/tasks/').then(function(response) {
			angular.copy(response.data.content, $scope.tasks);
		});
		$http.get('api/tasks/?completed=true').then(function(response) {
			angular.copy(response.data.content, $scope.completedTasks);
		});
	};

	$scope.addTask = function() {
		$http.post('api/tasks/', $scope.newTask).then(function(response) {
			$scope.newTask = '';
		});
	};

	$scope.starTask = function(task) {
		$http.post('api/tasks/' + task.id + '/star'); // IMO: should be patch, but not supported by this Angular version
	};

	$scope.unstarTask = function(task) {
		$http.post('api/tasks/' + task.id + '/unstar'); // IMO: should be patch, but not supported by this Angular version
	};

	$scope.completeTask = function(task) {
		$http.post('api/tasks/' + task.id + '/complete'); // IMO: should be patch, but not supported by this Angular version
	};

	$scope.showTaskDetails = function(task) {
		$scope.taskDetails = angular.copy(task);
	};

	$scope.modifyTaskTitle = function(task) {
		$http.post('api/tasks/' + task.id + '/title', {
			'title' : $scope.taskDetails.title
		}).then(function(response) {
			$scope.taskDetails = null;
		});
	};

	$scope.loadTasks();
}

function getTaskKey(tasks, id) {
	var taskKey;
	angular.forEach(tasks, function(value, key) {
		if (value.id == id) {
			taskKey = key;
		}
	});
	return taskKey;
}