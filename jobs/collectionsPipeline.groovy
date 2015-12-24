/*
    CI Pipeline - BUILD and DOCKERIZE
*/

deliveryPipelineView('CI-pipeline') {
    pipelineInstances(0)
    showAggregatedPipeline()
    columns(1)
    sorting(Sorting.TITLE)
    updateInterval(5)
    enableManualTriggers()
    showAvatars()
    showChangeLog()
    pipelines {
        regex(/^(.*-service)$/)
        regex(/^(.*-portal)$/)
    }
}
