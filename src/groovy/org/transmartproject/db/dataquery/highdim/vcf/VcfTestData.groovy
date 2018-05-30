/*
 * Copyright © 2013-2014 The Hyve B.V.
 *
 * This file is part of transmart-core-db.
 *
 * Transmart-core-db is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * transmart-core-db.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.transmartproject.db.dataquery.highdim.vcf

import groovy.util.logging.Slf4j
import org.transmartproject.db.AbstractTestData
import org.transmartproject.db.dataquery.highdim.DeGplInfo
import org.transmartproject.db.dataquery.highdim.DeSubjectSampleMapping
import org.transmartproject.db.dataquery.highdim.HighDimTestData
import org.transmartproject.db.dataquery.highdim.SampleBioMarkerTestData
import org.transmartproject.db.i2b2data.PatientDimension
import org.transmartproject.db.search.SearchKeywordCoreDb

/**
 * @author j.hudecek
 */
@Slf4j('logger')
class VcfTestData extends AbstractTestData {

	public static final String TRIAL_NAME = 'VCF_SAMP_TRIAL'

	DeGplInfo platform
	DeGplInfo otherPlatform
	DeVariantDatasetCoreDb dataset
	List<PatientDimension> patients
	List<DeSubjectSampleMapping> assays
	List<DeVariantSubjectSummaryCoreDb> summariesData
	List<DeVariantSubjectDetailCoreDb> detailsData
	List<DeVariantSubjectIdxCoreDb> indexData
	List<DeVariantPopulationDataCoreDb> populationData
	SampleBioMarkerTestData bioMarkerTestData

	VcfTestData(String conceptCode = 'bogus') {
		// Create VCF platform and assays
		platform = new DeGplInfo(
				title: 'Test VCF',
				organism: 'Homo Sapiens',
				markerType: 'VCF')
		platform.id = 'BOGUSGPLVCF'

		dataset = new DeVariantDatasetCoreDb(genome: 'human')
		dataset.id = 'BOGUSDTST'

		patients = HighDimTestData.createTestPatients(3, -800, TRIAL_NAME)

		assays = HighDimTestData.createTestAssays(patients, -1400, platform, TRIAL_NAME, conceptCode)

		// Create VCF data
		detailsData = []
		detailsData << createDetail(1, 'C', 'A', 'DP=88;AF1=1;QD=2;DP4=0,0,80,0;MQ=60;FQ=-268')
		detailsData << createDetail(2, 'GCCCCC', 'GCCCC', 'DP=88;AF1=1;QD=2;DP4=0,0,80,0;MQ=60;FQ=-268')
		detailsData << createDetail(3, 'A', 'C,T', 'DP=88;AF1=1;QD=2;DP4=0,0,80,0;MQ=60;FQ=-268')

		indexData = []
		assays.eachWithIndex { DeSubjectSampleMapping assay, int i ->
			indexData << new DeVariantSubjectIdxCoreDb(
					dataset: dataset,
					subjectId: assay.sampleCode,
					position: i + 1
			)
		}

		summariesData = []
		populationData = []
		for (DeVariantSubjectDetailCoreDb detail in detailsData) {
			// Create VCF summary entries with the following variants:
			// 1/0, 0/1 and 1/1
			int mut = 0
			assays.eachWithIndex { DeSubjectSampleMapping assay, int i ->
				mut++
				summariesData << createSummary(detail, mut & 1, (mut & 2) >> 1, assay, indexData[i], detail.pos == 1L)
			}
			if (detail.alt.contains(',')) {
				summariesData.last().allele1 = 2
			}

			// Create VCF population data entry
			if (detail.pos == 1 || detail.pos == 2) {
				populationData << createPopulationData(detail, 'GID', '-130751')
				populationData << createPopulationData(detail, 'GS', 'AURKA')
			}
		}

		// Add also another platform and assays for those patients
		// to test whether the VCF module only returns VCF assays
		otherPlatform = new DeGplInfo(
				title: 'Other platform',
				organism: 'Homo Sapiens',
				markerType: 'mrna')
		otherPlatform.id = 'BOGUSGPLMRNA'

		assays.addAll HighDimTestData.createTestAssays(patients, -1800, otherPlatform, 'OTHER_TRIAL')

		bioMarkerTestData = bioMarkerTestData ?: new SampleBioMarkerTestData()
	}

	@Lazy
	List<SearchKeywordCoreDb> searchKeywords = {
		bioMarkerTestData.geneSearchKeywords
	}()

	private DeVariantSubjectDetailCoreDb createDetail(int position, String reference,
	                                                  String alternative, String info) {
		new DeVariantSubjectDetailCoreDb(
				chr: 1,
				pos: position,
				rsId: '.',
				ref: reference,
				alt: alternative,
				quality: position, //nonsensical value
				filter: '.',
				info: info,
				format: 'GT',
				dataset: dataset,
				variant: String.valueOf(position) + '/' + position + '\t' +
						(position + 1) + '/' + (position + 1) + '\t' +
						(position * 2) + '/' + (position * 2)
		)
	}

	private DeVariantSubjectSummaryCoreDb createSummary(DeVariantSubjectDetailCoreDb detail, int allele1,
	                                                    int allele2, DeSubjectSampleMapping assay,
	                                                    DeVariantSubjectIdxCoreDb subjectIndex,
	                                                    boolean reference) {
		new DeVariantSubjectSummaryCoreDb(
				dataset: dataset,
				chr: 1,
				pos: detail.pos,
				rsId: '.',
				subjectId: subjectIndex.subjectId,
				variant: (allele1 == 0 ? detail.ref : detail.alt) + '/' + (allele2 == 0 ? detail.ref : detail.alt),
				variantFormat: (allele1 == 0 ? 'R' : 'V') + '/' + (allele2 == 0 ? 'R' : 'V'),
				variantType: detail.ref.length() > 1 ? 'DIV' : 'SNV',
				reference: reference,
				allele1: allele1,
				allele2: allele2,
				assay: assay,
				jDetail: detail,
				subjectIndex: subjectIndex)
	}

	private DeVariantPopulationDataCoreDb createPopulationData(DeVariantSubjectDetailCoreDb detail,
	                                                           String infoName, String textValue) {
		new DeVariantPopulationDataCoreDb(
				dataset: dataset,
				chromosome: 1,
				position: detail.pos,
				infoName: infoName,
				textValue: textValue)
	}

	void saveAll() {
		bioMarkerTestData.saveGeneData()

		save platform, logger
		save otherPlatform, logger
		save dataset, logger
		saveAll patients, logger
		saveAll assays, logger
		saveAll detailsData, logger
		saveAll indexData, logger
		saveAll summariesData, logger
		saveAll populationData, logger
	}
}
