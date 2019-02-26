package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootApplication
public class SalvoApplication {

	public static void main(String[] args) {

		SpringApplication.run(SalvoApplication.class, args);
	}


	@Bean
	public CommandLineRunner initData(PlayerRepository playerRepository, GameRepository gameRepository,
									  GamePlayerRepository gamePlayerRepository, ShipRepository shipRepository,
									  SalvoRepository salvoRepository, ScoreRepository scoreRepository) {
		return (args) -> {

			// PLAYERS

			Player p1 = new Player("j.bauer@ctu.gov", passwordEncoder().encode("24"));
			Player p2 = new Player("c.obrian@ctu.gov", passwordEncoder().encode("42"));
			Player p5 = new Player("kim_bauer@gmail.com", passwordEncoder().encode("kb"));
			Player p6 = new Player("t.almeida@ctu.gov", passwordEncoder().encode("mole"));

			playerRepository.save(p1);
			playerRepository.save(p2);
			playerRepository.save(p5);
			playerRepository.save(p6);

			// GAMES

			Date firstDate = new Date();
			Date secondDate = Date.from(firstDate.toInstant().plusSeconds(3600));
			Date thirdDate = Date.from(secondDate.toInstant().plusSeconds(3600));
			Date fourthDate = Date.from(thirdDate.toInstant().plusSeconds(3600));
			Date fifthDate = Date.from(fourthDate.toInstant().plusSeconds(3600));
			Date sixthDate = Date.from(fifthDate.toInstant().plusSeconds(3600));
			Date seventhDate = Date.from(sixthDate.toInstant().plusSeconds(3600));
			Date eigthDate = Date.from(seventhDate.toInstant().plusSeconds(3600));

			Game game1 = new Game(firstDate);
			Game game2 = new Game(secondDate);
			Game game3 = new Game(thirdDate);
			Game game4 = new Game(fourthDate);
			Game game5 = new Game(fifthDate);
			Game game6 = new Game(sixthDate);
			Game game7 = new Game(seventhDate);
			Game game8 = new Game(eigthDate);

			gameRepository.save(game1);
			gameRepository.save(game2);
			gameRepository.save(game3);
			gameRepository.save(game4);
			gameRepository.save(game5);
			gameRepository.save(game6);
			gameRepository.save(game7);
			gameRepository.save(game8);

			// GAMEPLAYER

			GamePlayer gp1 = new GamePlayer(game1, p1); //game1
			GamePlayer gp2 = new GamePlayer(game1, p2);
			GamePlayer gp3 = new GamePlayer(game2, p1); //game2
			GamePlayer gp4 = new GamePlayer(game2, p2);
			GamePlayer gp5 = new GamePlayer(game3, p2); //game3
			GamePlayer gp6 = new GamePlayer(game3, p6);
			GamePlayer gp7 = new GamePlayer(game4, p2); //game4
			GamePlayer gp8 = new GamePlayer(game4, p1);
			GamePlayer gp9 = new GamePlayer(game5, p6); //game5
			GamePlayer gp10 = new GamePlayer(game5, p1);
			GamePlayer gp11 = new GamePlayer(game6, p5); //game6
			//GamePlayer gp12 = new GamePlayer(game6, null);
			GamePlayer gp13 = new GamePlayer(game7, p6); //game7
			//GamePlayer gp14 = new GamePlayer(game7, null);
			GamePlayer gp15 = new GamePlayer(game8, p6); //game8
			GamePlayer gp16 = new GamePlayer(game8, p5);

			gamePlayerRepository.save(gp1);
			gamePlayerRepository.save(gp2);
			gamePlayerRepository.save(gp3);
			gamePlayerRepository.save(gp4);
			gamePlayerRepository.save(gp5);
			gamePlayerRepository.save(gp6);
			gamePlayerRepository.save(gp7);
			gamePlayerRepository.save(gp8);
			gamePlayerRepository.save(gp9);
			gamePlayerRepository.save(gp10);
			gamePlayerRepository.save(gp11);
			//gamePlayerRepository.save(gp12);
			gamePlayerRepository.save(gp13);
			//gamePlayerRepository.save(gp14);
			gamePlayerRepository.save(gp15);
			gamePlayerRepository.save(gp16);

			//SHIPS Y LOCATIONS

			//game1 player1
			/*List<String> location1 = new ArrayList<>();
			location1.add("H2");
			location1.add("H3");
			location1.add("H4");
			Ship sp1 = new Ship("destroyer", gp1, location1);

			List<String> location2 = new ArrayList<>();
			location2.add("E1");
			location2.add("F1");
			location2.add("G1");
			Ship sp2 = new Ship("submarine", gp1, location2);

			List<String> location3 = new ArrayList<>();
			location3.add("B4");
			location3.add("B5");
			Ship sp3 = new Ship("patrolboat", gp1, location3);

			//game1 player2
			List<String> location4 = new ArrayList<>();
			location4.add("B5");
			location4.add("C5");
			location4.add("D5");
			Ship sp4 = new Ship("destroyer", gp2, location4);

			List<String> location5 = new ArrayList<>();
			location5.add("F1");
			location5.add("F2");
			Ship sp5 = new Ship("patrolboat", gp2, location5);

			//game2 player1
			List<String> location6 = new ArrayList<>();
			location6.add("B5");
			location6.add("C5");
			location6.add("D5");
			Ship sp6 = new Ship("destroyer", gp3, location6);

			List<String> location7 = new ArrayList<>();
			location7.add("C6");
			location7.add("C7");
			Ship sp7 = new Ship("patrolboat", gp3, location7);

			//game2 player2
			List<String> location8 = new ArrayList<>();
			location8.add("A2");
			location8.add("A3");
			location8.add("A4");
			Ship sp8 = new Ship("submarine", gp4, location8);

			List<String> location9 = new ArrayList<>();
			location9.add("G6");
			location9.add("H6");
			Ship sp9 = new Ship("patrolboat", gp4, location9);

			shipRepository.save(sp1);
			shipRepository.save(sp2);
			shipRepository.save(sp3);
			shipRepository.save(sp4);
			shipRepository.save(sp5);
			shipRepository.save(sp6);
			shipRepository.save(sp7);
			shipRepository.save(sp8);
			shipRepository.save(sp9);

			//SALVOES

			//game1
			List<String> salvoLoc1 = new ArrayList<>();
			salvoLoc1.add("B5");
			salvoLoc1.add("C5");
			salvoLoc1.add("F1");
			Salvo salvo1 = new Salvo((long) 1, gp1, salvoLoc1);

			List<String> salvoLoc2 = new ArrayList<>();
			salvoLoc2.add("B4");
			salvoLoc2.add("B5");
			salvoLoc2.add("B6");
			Salvo salvo2 = new Salvo((long)1, gp2, salvoLoc2);

			List<String> salvoLoc3 = new ArrayList<>();
			salvoLoc3.add("F2");
			salvoLoc3.add("D5");
			Salvo salvo3 = new Salvo((long)2, gp1, salvoLoc3);

			List<String> salvoLoc4 = new ArrayList<>();
			salvoLoc4.add("E1");
			salvoLoc4.add("H3");
			salvoLoc4.add("A2");
			Salvo salvo4 = new Salvo((long)2, gp2, salvoLoc4);

			//game2
			List<String> salvoLoc5 = new ArrayList<>();
			salvoLoc5.add("A2");
			salvoLoc5.add("A4");
			salvoLoc5.add("G6");
			Salvo salvo5 = new Salvo((long) 1, gp3, salvoLoc5);

			List<String> salvoLoc6 = new ArrayList<>();
			salvoLoc6.add("B5");
			salvoLoc6.add("D5");
			salvoLoc6.add("C7");
			Salvo salvo6 = new Salvo((long)1, gp4, salvoLoc6);

			List<String> salvoLoc7 = new ArrayList<>();
			salvoLoc7.add("A3");
			salvoLoc7.add("H6");
			Salvo salvo7 = new Salvo((long)2, gp3, salvoLoc7);

			List<String> salvoLoc8 = new ArrayList<>();
			salvoLoc8.add("C5");
			salvoLoc8.add("C6");
			Salvo salvo8 = new Salvo((long)2, gp4, salvoLoc8);

			List<String> salvoLoc9 = new ArrayList<>();
			salvoLoc9.add("B2");
			salvoLoc9.add("B9");
			salvoLoc9.add("E1");
			salvoLoc9.add("E10");
			salvoLoc9.add("F2");
			salvoLoc9.add("F9");
			salvoLoc9.add("G3");
			salvoLoc9.add("G8");
			salvoLoc9.add("H4");
			salvoLoc9.add("H5");
			salvoLoc9.add("H6");
			salvoLoc9.add("H7");
			Salvo salvo9 = new Salvo((long) 1, gp5, salvoLoc9);

			salvoRepository.save(salvo1);
			salvoRepository.save(salvo2);
			salvoRepository.save(salvo3);
			salvoRepository.save(salvo4);
			salvoRepository.save(salvo5);
			salvoRepository.save(salvo6);
			salvoRepository.save(salvo7);
			salvoRepository.save(salvo8);
			salvoRepository.save(salvo9);

			//SCORES

			Date date2 = new Date();

			Score score1 = new Score(game1, p1, 1, date2);
			Score score2 = new Score(game1, p2, 0, date2);
			Score score3 = new Score(game2, p1, 0.5, new Date());
			Score score4 = new Score(game2, p2, 0.5, new Date());
			Score score5 = new Score(game3, p6, 1, new Date());
			Score score6 = new Score(game3, p2, 0, new Date());
			Score score7 = new Score(game4, p2, 0.5, new Date());
			Score score8 = new Score(game4, p1, 0.5, new Date());

			scoreRepository.save(score1);
			scoreRepository.save(score2);
			scoreRepository.save(score3);
			scoreRepository.save(score4);
			scoreRepository.save(score5);
			scoreRepository.save(score6);
			scoreRepository.save(score7);
			scoreRepository.save(score8);*/

		};


	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
}



@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

	@Autowired
	PlayerRepository playerRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Override
	public void init(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(inputName-> {
			Player player = playerRepository.findByUserName(inputName);
			if (player != null) {
				return new User(player.getUserName(), player.getPassword(),
						AuthorityUtils.createAuthorityList("USER"));
			} else {
				throw new UsernameNotFoundException("Unknown user: " + inputName);
			}
		});
	}
}

@EnableWebSecurity
@Configuration
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.antMatchers("/api/games").permitAll()
				.antMatchers("/api/players").permitAll()
				.antMatchers("/rest/*").denyAll()
				.antMatchers("/api/game_view/*").hasAnyAuthority("USER")
				.antMatchers("/web/**").permitAll()
				.antMatchers("/api/login").permitAll()
				.anyRequest().permitAll();

		http.formLogin()
				.usernameParameter("username")
				.passwordParameter("password")
				.loginPage("/api/login");

		http.logout().logoutUrl("/api/logout");

		// turn off checking for CSRF tokens
		http.csrf().disable();

		// if user is not authenticated, just send an authentication failure response
		http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		// if login is successful, just clear the flags asking for authentication
		http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));

		// if login fails, just send an authentication failure response
		http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		// if logout is successful, just send a success response
		http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
		}

		private void clearAuthenticationAttributes(HttpServletRequest request) {
			HttpSession session = request.getSession(false);
			if (session != null) {
				session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
			}
		}
}
