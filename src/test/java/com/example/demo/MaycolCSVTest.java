package com.example.demo;

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

    

}
