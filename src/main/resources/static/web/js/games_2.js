$(function() {
    loadData()
});

function updateViewGames(data) {
  var htmlList = data.map(function (games) {
      return  '<li class="list-group-item">' + new Date(games.creationDate).toLocaleString() + ' ' + games.gamePlayers.map(function(p) { return p.player.userName}).join(', ')  +'</li>';
  }).join('');
  document.getElementById("game-list").innerHTML = htmlList;
}

function updateViewLBoard(data) {
  var htmlList = data.map(function (score) {
                //console.log(score);
      return  '<tr><td>' + score.Name + '</td>'
              + '<td>' + score.Total + '</td>'
              + '<td>' + score.Won + '</td>'
              + '<td>' + score.Lost + '</td>'
              + '<td>' + score.Tied + '</td></tr>';
  }).join('');
  document.getElementById("leader-list").innerHTML = htmlList;
}

function loadData() {
  $.get("/api/games")
    .done(function(data) {
      updateViewGames(data);
    })
    .fail(function( jqXHR, textStatus ) {
      alert( "Failed: " + textStatus );
    });
  
  $.get("/api/leaderBoard")
    .done(function(data) {
      updateViewLBoard(data);
    })
    .fail(function( jqXHR, textStatus ) {
      alert( "Failed: " + textStatus );
    });
}