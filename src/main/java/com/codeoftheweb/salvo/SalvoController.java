package com.codeoftheweb.salvo;

import net.bytebuddy.implementation.bind.MethodDelegationBinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Id;
import java.lang.reflect.Array;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController

@RequestMapping("/api")
public class SalvoController {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    ScoreRepository scoreRepository;



    //////////---------GAMES----------///////////


    @RequestMapping("/games")
    public Map<String, Object> makeLoggedPlayer(Authentication authentication){
        Map<String, Object> dto = new LinkedHashMap<>();

        if(authentication == null || authentication instanceof AnonymousAuthenticationToken)
            dto.put("player", "Guest");
        else
            dto.put("player", loggedPlayerDTO(playerRepository.findByUserName(authentication.getName())));
        dto.put("games", getAllgames());
        return dto;
    }

    public Map<String, Object> loggedPlayerDTO(Player player){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", player.getId());
        dto.put("name", player.getUserName());
        return dto;
    }


    @RequestMapping(path = "/games", method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> createGame(Authentication authentication) {

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken){
            return new ResponseEntity<>(makeMap("error","Unauthorized"), HttpStatus.UNAUTHORIZED);
        }

        //creo un nuevo juego y gameplayer asociado
        Game newGame = gameRepository.save(new Game(new Date()));
        GamePlayer newGP = new GamePlayer(newGame, playerRepository.findByUserName(authentication.getName()));

        gamePlayerRepository.save(newGP);

        return new ResponseEntity<>(makeMap("gpid", newGP.getId()), HttpStatus.CREATED);

    }

    private Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }


    @RequestMapping(path = "/game/{gameid}/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> joinGame(Authentication authentication, @PathVariable long gameid){

        //quise poner prints para ver si llegaba correctamente, y pasa lo que tiene que pasar pero no se ven los prints
        //(excepto el último). creo que es porque en el js se pregunta cada caso y no me puede aparecer la opción
        //de join game si no se cumplen las condiciones de por sí.

        if (authentication.getName().isEmpty()){
            return new ResponseEntity<>(makeMap("error","Unauthorized"), HttpStatus.UNAUTHORIZED);
        }

        Game game = gameRepository.findById(gameid).orElse(null);
        if (game == null) {
            return new ResponseEntity<>(makeMap("error", "No such game"), HttpStatus.FORBIDDEN);
        }

        long playersCount = gameRepository.findAll().stream().filter(g -> g.getId() == gameid).count();
        //if(gameRepository.findById(gameid).get().getGamePlayers().size() == 2){
        if(playersCount == 2){
            return new ResponseEntity<>(makeMap("error", "Game is full"), HttpStatus.FORBIDDEN);
        }


        GamePlayer newGP = gamePlayerRepository.save(new GamePlayer(gameRepository.findById(gameid).get(),
                playerRepository.findByUserName(authentication.getName())));

        return new ResponseEntity<>(makeMap("gpid", newGP.getId()), HttpStatus.CREATED);

    }


    public List<Object> getAllgames() {
        return gameRepository
                .findAll()
                .stream()
                .map(games -> makeGameDTO(games))
                .collect(Collectors.toList());
    }

    private Map<String, Object> makeGameDTO(Game game) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", game.getId());
        dto.put("creationDate", game.getCreationDate().getTime());
        dto.put("gamePlayers", getAllGamePlayers(game.getGamePlayers()));
        dto.put("scores", getAllScores(game.getScores()));
        return dto;
    }

    public List<Object> getAllGamePlayers(Set<GamePlayer> gamePlayers) {
        return gamePlayers
                .stream()
                .map(gamePlayer -> makeGamePlayerDTO(gamePlayer))
                .collect(Collectors.toList());
    }

    private Map<String, Object> makeGamePlayerDTO(GamePlayer gamePlayer){
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("gpid", gamePlayer.getId());
        //dto.put("id", gamePlayer.getPlayer().getId());
        //dto.put("name", gamePlayer.getPlayer().getUserName());
        dto.put("player", makePlayerDTO(gamePlayer.getPlayer()));
        return dto;
    }

    private Map<String, Object> makePlayerDTO(Player player){
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", player.getId());
        dto.put("userName", player.getUserName());
        return dto;
    }

    public List<Object> getAllScores(List<Score> scores){
        return scores
                .stream()
                .map(score -> makeScoreDTO(score))
                .collect(Collectors.toList());
    }

    private Map<String, Object> makeScoreDTO(Score score){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("player", score.getPlayer().getId());
        dto.put("score", score.getScore());
        dto.put("finishDate", score.getFinishDate()); //preguntar por .getTime()
        return dto;
    }

    //////////------PLAYERS--------//////////

    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Object> register(@RequestParam String userName, @RequestParam String password) {
        if (userName.isEmpty()) {
            return new ResponseEntity<>("Missing data", HttpStatus.FORBIDDEN);
        }
        Player player = playerRepository.findByUserName(userName);
        if (player != null) {
            return new ResponseEntity<>("Name already in use", HttpStatus.FORBIDDEN);
        }

        Player newPlayer = playerRepository.save(new Player(userName, passwordEncoder.encode(password)));
        return new ResponseEntity<>(makeMap("id", newPlayer.getId()), HttpStatus.CREATED);
    }

    //////////------GAME_VIEW--------//////////

    Player getLoggedPlayer(Authentication authentication){
        return playerRepository.findByUserName(authentication.getName());
    }

    @RequestMapping("/game_view/{gpid}")
    public ResponseEntity<Object> gameView(@PathVariable long gpid, Authentication authentication) {
        Player player = getLoggedPlayer(authentication);
        GamePlayer gamePlayer = gamePlayerRepository.findById(gpid).orElse(null);
        if (gamePlayer == null) {
            return new ResponseEntity<>(makeMap("error", "Forbidden"), HttpStatus.FORBIDDEN);
        }

        if (player.getId() != gamePlayer.getPlayer().getId()) {
            return new ResponseEntity<>(makeMap("error", "Unauthorized"), HttpStatus.UNAUTHORIZED);
        }

        GamePlayer opponentGP = gamePlayer.getGame().getGamePlayers().stream().filter(gpo -> gpo.getId() != gpid).findFirst().orElse(new GamePlayer());
        GamePlayer opponentGamePlayer = opponentGP;

        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", gamePlayer.getGame().getId());
        dto.put("creationDate", gamePlayer.getGame().getCreationDate().getTime());
        dto.put("gameState", getGameState(gamePlayer, opponentGamePlayer));
        dto.put("gamePlayers", getAllGamePlayers(gamePlayer.getGame().getGamePlayers()));
        dto.put("ships", getAllShips(gamePlayer.getShips()));
        dto.put("salvoes", getSalvoList(gamePlayer.getGame()));
        dto.put("hits", selfAndOpponent(gamePlayer, opponentGamePlayer));

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    public List<Object> getAllShips(Set<Ship> ships) {
        return ships
                .stream()
                .map(ship -> makeShipDTO(ship))
                .collect(Collectors.toList());
    }

    private Map<String, Object> makeShipDTO(Ship ship){
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("type", ship.getType());
        dto.put("locations", ship.getLocations());
        return dto;
    }

    private List<Map<String, Object>> getSalvoList(Game game){
        List <Map<String, Object>> myList = new ArrayList<>();
        game.getGamePlayers().forEach(gamePlayer -> myList.addAll(makeSalvoList(gamePlayer.getSalvoes())));
        return myList;
    }

    public List <Map<String, Object>> makeSalvoList(Set<Salvo> salvoes){
        return salvoes
                .stream()
                .map(salvo -> makeSalvoDTO(salvo))
                .collect(Collectors.toList());
    }

    private Map<String, Object> makeSalvoDTO(Salvo salvo){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("turn", salvo.getTurn());
        dto.put("player", salvo.getGamePlayer().getPlayer().getId());
        dto.put("locations", salvo.getLocations());
        return dto;
    }

    ////////-------SHIPS-------////////

    @Autowired
    private ShipRepository shipRepository;

    @RequestMapping(path = "/games/players/{gamePlayerID}/ships", method = RequestMethod.POST)
    public ResponseEntity<Object> createShips(@PathVariable long gamePlayerID, @RequestBody Set<Ship> ships,
                                              Authentication authentication){

        //si no está logueado
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken){
            return new ResponseEntity<>(makeMap("error", "Please, log in first"), HttpStatus.UNAUTHORIZED);
        }

        //si no existe el gpID
        GamePlayer gamePlayer = gamePlayerRepository.findById(gamePlayerID).orElse(null);
        if (gamePlayer == null){
            return new ResponseEntity<>(makeMap("error", "The ID doesn't exist"), HttpStatus.UNAUTHORIZED);
        }

        //si no coincide el id con el gamePlayer
        Player player = getLoggedPlayer(authentication);
        if(player.getId() != gamePlayer.getPlayer().getId()){
            return new ResponseEntity<>(makeMap("error", "You shouldn't do this"), HttpStatus.UNAUTHORIZED);
        }

        if(gamePlayer.getShips().isEmpty()){
            for(Ship ship : ships){
                ship.setGamePlayer(gamePlayer);
                shipRepository.save(ship);
            }
            //shipRepository.saveAll(ships);
            return new ResponseEntity<>(makeMap("yay", "Ships located!"), HttpStatus.CREATED);
        } else {
            //si el player ya seteó sus ships
            return new ResponseEntity<>(makeMap("error", "Ships have already been located"), HttpStatus.FORBIDDEN);
        }


    }

    ////////-------SALVOES-------////////

    @Autowired
    private SalvoRepository salvoRepository;

    @RequestMapping(path = "/games/players/{gamePlayerID}/salvoes", method = RequestMethod.POST)
    public ResponseEntity<Object> fireSalvo(@PathVariable long gamePlayerID, @RequestBody Salvo salvo,
                                            Authentication authentication) {

        //si no está logueado
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return new ResponseEntity<>(makeMap("error", "Please, log in first"), HttpStatus.UNAUTHORIZED);
        }

        //si no existe el gpID
        GamePlayer gamePlayer = gamePlayerRepository.findById(gamePlayerID).orElse(null);
        if (gamePlayer == null) {
            return new ResponseEntity<>(makeMap("error", "The ID doesn't exist"), HttpStatus.UNAUTHORIZED);
        }

        //si no coincide el id con el gamePlayer
        Player player = getLoggedPlayer(authentication);
        if (player.getId() != gamePlayer.getPlayer().getId()) {
            return new ResponseEntity<>(makeMap("error", "You shouldn't do this"), HttpStatus.UNAUTHORIZED);
        }


        List<Salvo> salvos = salvoRepository.findByTurn(salvo.getTurn()); //todos los salvos con ese turno
        long gpCount = salvos.stream().filter(s -> s.getGamePlayer().getId() == gamePlayerID).count(); //se fija si coincide el gpID con alguno que ya haya disparado

        //se dispara el salvo
        if (salvos.isEmpty() || gpCount == 0) { //si todavía ese gameplayer no disparó en ese turno, dispara
            salvo.setGamePlayer(gamePlayer);
            salvo.setTurn(gamePlayer.getSalvoes().size()+1);
            salvoRepository.save(salvo);
            return new ResponseEntity<>(makeMap("Done", "Salvo fired"), HttpStatus.CREATED);
        } else {
            //si el jugador ya disparó en este turno
            return new ResponseEntity<>(makeMap("error", "Your turn has passed"), HttpStatus.FORBIDDEN);
        }
    }

    ////////-------LEADERBOARD-------////////


    @RequestMapping("/leaderBoard")
    public List<Object> getAllPlayers() {
        return playerRepository
                .findAll()
                .stream()
                .map(player -> playerScoreDTO(player))
                .collect(Collectors.toList());
    }

    private Map<String, Object> playerScoreDTO(Player player){
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("Name", player.getUserName());
        dto.put("Total", player.getTotalScore(player.getScores()));
        dto.put("Won", player.getWins(player.getScores()));
        dto.put("Lost", player.getLosses(player.getScores()));
        dto.put("Tied", player.getTies(player.getScores()));
        return dto;
    }

    ////////-------HITS-------////////

    private Map<String, Object> selfAndOpponent(GamePlayer gamePlayer, GamePlayer opponentGamePlayer){
        Map<String, Object> dtoSelfAndOpponent = new LinkedHashMap<>();
        dtoSelfAndOpponent.put("self", getHits(gamePlayer, opponentGamePlayer));
        dtoSelfAndOpponent.put("opponent", getHits(opponentGamePlayer, gamePlayer));
        return dtoSelfAndOpponent;
    }

    private List<Map> getHits(GamePlayer gamePlayer, GamePlayer opponentGamePlayer) {

        List<Map> hits = new ArrayList<>();
        Integer carrierDamage = 0;
        Integer battleshipDamage = 0;
        Integer submarineDamage = 0;
        Integer destroyerDamage = 0;
        Integer patrolboatDamage = 0;
        List<String> carrierLocation = new ArrayList<>();
        List<String> battleshipLocation = new ArrayList<>();
        List<String> submarineLocation = new ArrayList<>();
        List<String> destroyerLocation = new ArrayList<>();
        List<String> patrolboatLocation = new ArrayList<>();
        if (gamePlayer.getShips().isEmpty()){
            return hits;
        }
            gamePlayer.getShips().forEach(ship -> {
                switch (ship.getType()) {
                    case "carrier":
                        carrierLocation.addAll(ship.getLocations());
                        break;
                    case "battleship":
                        battleshipLocation.addAll(ship.getLocations());
                        break;
                    case "submarine":
                        submarineLocation.addAll(ship.getLocations());
                        break;
                    case "destroyer":
                        destroyerLocation.addAll(ship.getLocations());
                        break;
                    case "patrolboat":
                        patrolboatLocation.addAll(ship.getLocations());
                        break;
                }
            });

            if(opponentGamePlayer.getSalvoes().isEmpty()){
                return hits;
            }

            for(Salvo salvo : opponentGamePlayer.getSalvoes()){
                Integer carrierHits = 0;
                Integer battleshipHits = 0;
                Integer submarineHits = 0;
                Integer destroyerHits = 0;
                Integer patrolboatHits = 0;
                Integer missed = salvo.getLocations().size();
                Map<String, Object> hitsMapPerTurn = new LinkedHashMap<>();
                Map<String, Object> damagesMapPerTurn = new LinkedHashMap<>();
                List<String> salvoLocationsList = new ArrayList<>();
                salvoLocationsList.addAll(salvo.getLocations());
                List<String> hitCellsList = new ArrayList<>();

                for (String salvoShot : salvoLocationsList) {
                    if (carrierLocation.contains(salvoShot)) {
                        carrierDamage++;
                        carrierHits++;
                        hitCellsList.add(salvoShot);
                        missed--;
                    }
                    if (battleshipLocation.contains(salvoShot)) {
                        battleshipDamage++;
                        battleshipHits++;
                        hitCellsList.add(salvoShot);
                        missed--;
                    }
                    if (submarineLocation.contains(salvoShot)) {
                        submarineDamage++;
                        submarineHits++;
                        hitCellsList.add(salvoShot);
                        missed--;
                    }
                    if (destroyerLocation.contains(salvoShot)) {
                        destroyerDamage++;
                        destroyerHits++;
                        hitCellsList.add(salvoShot);
                        missed--;
                    }
                    if (patrolboatLocation.contains(salvoShot)) {
                        patrolboatDamage++;
                        patrolboatHits++;
                        hitCellsList.add(salvoShot);
                        missed--;
                    }
                }

                    damagesMapPerTurn.put("carrierHits", carrierHits);
                    damagesMapPerTurn.put("battleshipHits", battleshipHits);
                    damagesMapPerTurn.put("submarineHits", submarineHits);
                    damagesMapPerTurn.put("destroyerHits", destroyerHits);
                    damagesMapPerTurn.put("patrolboatHits", patrolboatHits);
                    damagesMapPerTurn.put("carrier", carrierDamage);
                    damagesMapPerTurn.put("battleship", battleshipDamage);
                    damagesMapPerTurn.put("submarine", submarineDamage);
                    damagesMapPerTurn.put("destroyer", destroyerDamage);
                    damagesMapPerTurn.put("patrolboat", patrolboatDamage);

                    hitsMapPerTurn.put("turn", salvo.getTurn());
                    hitsMapPerTurn.put("hitLocations", hitCellsList);
                    hitsMapPerTurn.put("damages", damagesMapPerTurn);
                    hitsMapPerTurn.put("missed", missed);

                    hits.add(hitsMapPerTurn);
                }

        return hits;

    }

    ////////-------GAME-STATE-------////////


    private int getCurrentTurn(GamePlayer selfGP, GamePlayer opponentGP){
        int selfGPSalvoes = selfGP.getSalvoes().size();
        int opponentGPSalvoes = opponentGP.getSalvoes().size();

        int totalSalvoes = selfGPSalvoes + opponentGPSalvoes;

        if(totalSalvoes % 2 == 0)
            return totalSalvoes / 2 + 1;

        return (int) (totalSalvoes / 2.0 + 0.5);
    }

    private boolean allShipsSunk(GamePlayer gamePlayer, GamePlayer opponentGameplayer) {

        int carrierDamage = 0;
        int battleshipDamage = 0;
        int submarineDamage = 0;
        int destroyerDamage = 0;
        int patrolboatDamage = 0;
        List<String> carrierLocation = new ArrayList<>();
        List<String> battleshipLocation = new ArrayList<>();
        List<String> submarineLocation = new ArrayList<>();
        List<String> destroyerLocation = new ArrayList<>();
        List<String> patrolboatLocation = new ArrayList<>();
        gamePlayer.getShips().forEach(ship -> {
            switch (ship.getType()) {
                case "carrier":
                    carrierLocation.addAll(ship.getLocations());
                    break;
                case "battleship":
                    battleshipLocation.addAll(ship.getLocations());
                    break;
                case "submarine":
                    submarineLocation.addAll(ship.getLocations());
                    break;
                case "destroyer":
                    destroyerLocation.addAll(ship.getLocations());
                    break;
                case "patrolboat":
                    patrolboatLocation.addAll(ship.getLocations());
                    break;
            }
        });

        for (Salvo salvo : opponentGameplayer.getSalvoes()) {

            List<String> salvoLocationsList = new ArrayList<>();
            salvoLocationsList.addAll(salvo.getLocations());

            for (String salvoShot : salvoLocationsList) {
                if (carrierLocation.contains(salvoShot)) {
                    carrierDamage++;
                }
                if (battleshipLocation.contains(salvoShot)) {
                    battleshipDamage++;
                }
                if (submarineLocation.contains(salvoShot)) {
                    submarineDamage++;
                }
                if (destroyerLocation.contains(salvoShot)) {
                    destroyerDamage++;
                }
                if (patrolboatLocation.contains(salvoShot)) {
                    patrolboatDamage++;
                }
            }
        }

        boolean carrierSunk = false;
        boolean battleshipSunk = false;
        boolean submarineSunk = false;
        boolean destroyerSunk = false;
        boolean patrolboatSunk = false;

        if (carrierDamage == 5){
            carrierSunk = true;
        }
        if (battleshipDamage == 4){
            battleshipSunk = true;
        }
        if (submarineDamage == 3){
            submarineSunk = true;
        }
        if (destroyerDamage == 3){
            destroyerSunk = true;
        }
        if(patrolboatDamage == 2){
            patrolboatSunk = true;
        }

        return carrierSunk && battleshipSunk && submarineSunk && destroyerSunk && patrolboatSunk;

    }


    private boolean existScore(Score score, Game game){
        List<Score> scores = game.getScores();
        for(Score s : scores){
            if(score.getPlayer().getUserName().equals(s.getPlayer().getUserName())){
                return true;
            }
        }
        return false;
    }


    private String getGameState (GamePlayer gamePlayer, GamePlayer opponent) {
        Set<Ship> selfShips = gamePlayer.getShips();
        Set<Salvo> selfSalvoes = gamePlayer.getSalvoes();
        if (selfShips.size() == 0) {
            return "PLACESHIPS";
        }

        if (opponent.getShips() == null){
            return "WAITINGFOROPP";
        }

        if (opponent.getShips().isEmpty()) {
            return "WAIT";
        }

        int turn = getCurrentTurn(gamePlayer, opponent);
        Set<Salvo> opponentSalvoes = opponent.getSalvoes();
        Player self = gamePlayer.getPlayer();
        Game game = gamePlayer.getGame();

        if (selfSalvoes.size() == opponentSalvoes.size()) {
            if (allShipsSunk(gamePlayer, opponent) && allShipsSunk(opponent, gamePlayer)) {
                Score score = new Score(game, self, 0.5, new Date());
                if (!existScore(score, game)) {
                    scoreRepository.save(score);
                }
                return "TIE";
            }
            if (allShipsSunk(gamePlayer, opponent)) {
                Score score = new Score(game, self, 0, new Date());
                if (!existScore(score, game)) {
                    scoreRepository.save(score);
                }
                return "LOST";
            }
            if (allShipsSunk(opponent, gamePlayer)) {
                Score score = new Score(game, self, 1, new Date());
                if (!existScore(score, game)) {
                    scoreRepository.save(score);
                }
                return "WON";
            }
        }
        if (selfSalvoes.size() != turn) {
            return "PLAY";
        }
        return "WAIT";
    }

}