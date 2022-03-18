package com.example.demo;

import jdk.jfr.FlightRecorder;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class MaycolCSVTest {

    @Test
    void converterData(){
        List<Player> list = CsvUtilFile.getPlayers();
        assert list.size() == 18207;
    }


    //FILTRO DE JUGADORES MAYORES DE 34 AÑOS POR STREAM
    @Test
    void stream_playersFilterAgeOver34() {
        List<Player> list = CsvUtilFile.getPlayers();
        Map<String, List<Player>> listFilter = list.parallelStream()
                .filter(player -> player.getAge() >= 34) //mayores o iguales a 34
                .map(player -> {
                    player.name = player.name.toUpperCase(Locale.ROOT);
                    return player;
                })
                .flatMap(playerA -> list.parallelStream() //filtra a los jugadores por club
                        .filter(playerB -> playerA.club.equals(playerB.club))
                )
                .distinct()
                .collect(Collectors.groupingBy(Player::getClub));

        System.out.println(listFilter.size());
//        listFilter.entrySet().stream().forEach(System.out::println);
        assert listFilter.size() == 451;
    }

    //FILTRO DE JUGADORES MAYORES DE 34 AÑOS POR REACTIVE
    @Test
    void reactive_playersFilterAgeOver34() {
        List<Player> list = CsvUtilFile.getPlayers();
        Flux<Player> listFlux = Flux.fromStream(list.parallelStream()).cache();
        Mono<Map<String, Collection<Player>>> listFilter = listFlux.filter(player -> player.age >= 34) //mayores a 34 años
                .map(player -> {
                    player.name = player.name.toUpperCase(Locale.ROOT);
                    return player;
                })
                .buffer(100) //collect of 100 elements
                .flatMap(playerA -> listFlux
                        .filter(playerB -> playerA.stream()
                                .anyMatch(a -> a.club.equals(playerB.club)))
                )
                .distinct()
                .collectMultimap(Player::getClub);

        System.out.println(listFilter.block().size());

        assert listFilter.block().size() == 451;

    }

    //PRUEBA REACTIVE DE JUGADORES ORGANIZADOS POR NACIONALIDAD EN UN MAP
    @Test
    void reactive_nationalityPlayers() {
        List<Player> list = CsvUtilFile.getPlayers();
        Flux<Player> listFlux = Flux.fromStream(list.parallelStream()).cache();
        Mono<Map<String, Collection<Player>>> listNational = listFlux
                .buffer(100)
                .flatMap(player1 -> listFlux
                        .filter(player2 -> player1.stream()
                                .anyMatch(n -> n.national.equals(player2.national)))
//                        .sort()
                )
                .distinct()
                .collectMultimap(Player::getNational);

        //LISTA DE NACIONALIDADES
        List<String> nationalities = listNational.block().keySet().stream().collect(Collectors.toList()); //lista de nacionalidades
        //System.out.println(nationalities);


        System.out.println(listNational.block().size()); //obtiene el tamaño
        //System.out.println(listNational.block().keySet()); //obteniene las nacionalidades
        //System.out.println(listNational.block().values().stream().findAny()); //obtiene los gugadores con la primera nacionalidad
        System.out.println(listNational.block().get("Argentina").size());


        assert listNational.block().size() == 164;
        assert listNational.block().get("Argentina").size() == 937; //jugadores de Argentina
    }

}
