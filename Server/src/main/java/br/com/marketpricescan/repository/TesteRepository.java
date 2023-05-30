package br.com.marketpricescan.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import br.com.marketpricescan.model.Teste;

@RepositoryRestResource(collectionResourceRel = "teste", path = "teste")
public interface TesteRepository extends PagingAndSortingRepository<Teste, Long> {
    
}
