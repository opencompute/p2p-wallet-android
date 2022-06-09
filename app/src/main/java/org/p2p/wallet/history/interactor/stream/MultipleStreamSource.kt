package org.p2p.wallet.history.interactor.stream

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

class MultipleStreamSource(
    private val sources: List<HistoryStreamSource>
) : AbstractStreamSource() {
    private val buffer = mutableListOf<HistoryStreamItem>()
    private val executor =
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    override suspend fun currentItem(): HistoryStreamItem? =
        withContext(executor + CoroutineExceptionHandler { _, _ -> }) {
            var maxValue: HistoryStreamItem?

            val items = sources.map {
                async(this.coroutineContext) { it.currentItem() }
            }
                .awaitAll()
                .filterNotNull()

            if (items.isEmpty()) {
                return@withContext null
            }

            maxValue = items.firstOrNull()
            for (item in items) {
                if (maxValue?.streamSource!!.blockTime <= item.streamSource!!.blockTime) {
                    maxValue = item
                }
            }
            return@withContext maxValue
        }

    override suspend fun next(configuration: StreamSourceConfiguration): HistoryStreamItem? {
        if (buffer.isEmpty()) {
            fillBuffer(configuration)
        }
        if (buffer.isEmpty()) {
            return null
        }
        return buffer.removeAt(0)
    }

    override fun reset() {
        buffer.clear()
        sources.forEach { it.reset() }
    }

    private suspend fun fillBuffer(configuration: StreamSourceConfiguration) = supervisorScope {
        val items = sources.map {
            async { it.nextItems(configuration) }
        }.awaitAll().flatten()
        val sortedItems = items.sortedWith(compareBy { it.streamSource?.blockTime }).asReversed()
        buffer.addAll(sortedItems)
    }
}