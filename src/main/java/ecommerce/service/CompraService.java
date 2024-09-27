package ecommerce.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import ecommerce.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ecommerce.dto.CompraDTO;
import ecommerce.dto.DisponibilidadeDTO;
import ecommerce.dto.EstoqueBaixaDTO;
import ecommerce.dto.PagamentoDTO;
import ecommerce.external.IEstoqueExternal;
import ecommerce.external.IPagamentoExternal;
import jakarta.transaction.Transactional;

@Service
public class CompraService {

	private final CarrinhoDeComprasService carrinhoService;
	private final ClienteService clienteService;

	private final IEstoqueExternal estoqueExternal;
	private final IPagamentoExternal pagamentoExternal;

	@Autowired
	public CompraService(CarrinhoDeComprasService carrinhoService, ClienteService clienteService,
			IEstoqueExternal estoqueExternal, IPagamentoExternal pagamentoExternal) {
		this.carrinhoService = carrinhoService;
		this.clienteService = clienteService;

		this.estoqueExternal = estoqueExternal;
		this.pagamentoExternal = pagamentoExternal;
	}

	@Transactional
	public CompraDTO finalizarCompra(Long carrinhoId, Long clienteId) {
		Cliente cliente = clienteService.buscarPorId(clienteId);
		CarrinhoDeCompras carrinho = carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente);

		List<Long> produtosIds = carrinho.getItens().stream().map(i -> i.getProduto().getId())
				.collect(Collectors.toList());
		List<Long> produtosQtds = carrinho.getItens().stream().map(i -> i.getQuantidade()).collect(Collectors.toList());

		DisponibilidadeDTO disponibilidade = estoqueExternal.verificarDisponibilidade(produtosIds, produtosQtds);

		if (!disponibilidade.disponivel()) {
			throw new IllegalStateException("Itens fora de estoque.");
		}

		BigDecimal custoTotal = calcularCustoTotal(carrinho);

		PagamentoDTO pagamento = pagamentoExternal.autorizarPagamento(cliente.getId(), custoTotal.doubleValue());

		if (!pagamento.autorizado()) {
			throw new IllegalStateException("Pagamento não autorizado.");
		}

		EstoqueBaixaDTO baixaDTO = estoqueExternal.darBaixa(produtosIds, produtosQtds);

		if (!baixaDTO.sucesso()) {
			pagamentoExternal.cancelarPagamento(cliente.getId(), pagamento.transacaoId());
			throw new IllegalStateException("Erro ao dar baixa no estoque.");
		}

		CompraDTO compraDTO = new CompraDTO(true, pagamento.transacaoId(), "Compra finalizada com sucesso.");

		return compraDTO;
	}

	public BigDecimal calcularCustoTotal(CarrinhoDeCompras carrinho) {
		BigDecimal custoProdutos = BigDecimal.ZERO;
		BigDecimal pesoTotal = BigDecimal.ZERO;

		// Calculando o custo total dos produtos e o peso total
		for (ItemCompra item : carrinho.getItens()) {
			Produto produto = item.getProduto();
			BigDecimal precoProduto = produto.getPreco();
			BigDecimal pesoProduto = BigDecimal.valueOf(produto.getPeso());


			custoProdutos = custoProdutos.add(precoProduto.multiply(BigDecimal.valueOf(item.getQuantidade())));

			pesoTotal = pesoTotal.add(pesoProduto.multiply(BigDecimal.valueOf(item.getQuantidade())));
		}

		// Aplicar desconto
		if (custoProdutos.compareTo(BigDecimal.valueOf(1000)) > 0) {
			custoProdutos = custoProdutos.multiply(BigDecimal.valueOf(0.80)); // Desconto de 20%
		} else if (custoProdutos.compareTo(BigDecimal.valueOf(500)) > 0) {
			custoProdutos = custoProdutos.multiply(BigDecimal.valueOf(0.90)); // Desconto de 10%
		}

		BigDecimal frete = calcularFrete(pesoTotal);

		if (carrinho.getCliente().getTipo() == TipoCliente.OURO) {
			frete = BigDecimal.ZERO; //
		} else if (carrinho.getCliente().getTipo() == TipoCliente.PRATA) {
			frete = frete.multiply(BigDecimal.valueOf(0.50));
		}

		return custoProdutos.add(frete);
	}

	private BigDecimal calcularFrete(BigDecimal pesoTotal) {
		if (pesoTotal.compareTo(BigDecimal.valueOf(5)) <= 0) {
			return BigDecimal.ZERO; // Até 5kg, frete grátis
		} else if (pesoTotal.compareTo(BigDecimal.valueOf(10)) <= 0) {
			return pesoTotal.multiply(BigDecimal.valueOf(2)); // De 5kg até 10kg, R$ 2,00 por kg
		} else if (pesoTotal.compareTo(BigDecimal.valueOf(50)) <= 0) {
			return pesoTotal.multiply(BigDecimal.valueOf(4)); // De 10kg até 50kg, R$ 4,00 por kg
		} else {
			return pesoTotal.multiply(BigDecimal.valueOf(7)); // Acima de 50kg, R$ 7,00 por kg
		}
	}
}
