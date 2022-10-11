package me.dio.sacola.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import me.dio.sacola.enumaration.FormaPagamento;
import me.dio.sacola.model.Item;
import me.dio.sacola.model.Restaurante;
import me.dio.sacola.model.Sacola;
import me.dio.sacola.repository.ItemRepository;
import me.dio.sacola.repository.ProdutoRepository;
import me.dio.sacola.repository.SacolaRepository;
import me.dio.sacola.resource.dto.ItemDto;
import me.dio.sacola.service.SacolaService;

@Service
public class SacolaServiceImpl implements SacolaService {

	@Autowired
	private SacolaRepository sacolaRepository;
	
	@Autowired
	private ProdutoRepository produtoRepository;
	
	@Autowired
	private ItemRepository itemRepository;
	
	@Override
	public Sacola verSacola(Long id) {
		return sacolaRepository.findById(id).orElseThrow(
				() -> {
					throw new RuntimeException("Essa sacola não existe");
					
				});
	}

	@Override
	public Sacola fechaSacola(Long id, int numeroFomaPagamento) {
		Sacola sacola = verSacola(id);
		
		if(sacola.getItens().isEmpty()) {
			throw new  RuntimeException("Inclua itens na sacola");
		}
		//if ternario
		FormaPagamento formaPagamento  = numeroFomaPagamento == 0 ? FormaPagamento.DINHEIRO : FormaPagamento.MAQUINETA;
		
		sacola.setFormaPagamento(formaPagamento); 
		sacola.setFechado(true);
		return sacolaRepository.save(sacola);
	}

	@Override
	public Item incluirItemNaSacola(ItemDto itemDto) {
		
		Sacola sacola = verSacola(itemDto.getIdSacola());
		
		if(sacola.isFechado()) {
			throw new RuntimeException("Esta sacola esta fechada");
		}
		
		Item itemParaSerInserido = Item.builder()
		.quantidade(itemDto.getQuantidade())
		.sacola(sacola)
		.produto(produtoRepository.findById(itemDto.getIdProduto()).orElseThrow(
				() -> {
					throw new RuntimeException("Esse Produto não existe");
					
				})
				)
		
		.build();
		
		List<Item> itensDaSacola = sacola.getItens();
		if(itensDaSacola.isEmpty()) {
			itensDaSacola.add(itemParaSerInserido);
		} else {
			
			Restaurante restauranteAtual = itensDaSacola.get(0).getProduto().getRestaurante();
			Restaurante restauranteDoItemParaAdicionar = itemParaSerInserido.getProduto().getRestaurante();
			
			if(restauranteAtual.equals(restauranteDoItemParaAdicionar)) {
				itensDaSacola.add(itemParaSerInserido);
			}else {
				throw new RuntimeException("Não é possivel adicionar produtos de restaurantes diferentes.");
			}
		}
		
		sacolaRepository.save(sacola);
		return itemRepository.save(itemParaSerInserido);
	}

}
