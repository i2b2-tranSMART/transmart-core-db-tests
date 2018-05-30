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

package org.transmartproject.db.dataquery.highdim.mirna

import groovy.util.logging.Slf4j
import org.transmartproject.db.AbstractTestData
import org.transmartproject.db.dataquery.highdim.DeGplInfo
import org.transmartproject.db.dataquery.highdim.DeSubjectSampleMapping
import org.transmartproject.db.dataquery.highdim.HighDimTestData
import org.transmartproject.db.dataquery.highdim.SampleBioMarkerTestData
import org.transmartproject.db.i2b2data.PatientDimension
import org.transmartproject.db.ontology.ConceptTestData

@Slf4j('logger')
class MirnaTestData extends AbstractTestData {

	public static final String TRIAL_NAME = 'MIRNA_SAMP_TRIAL'

	String typeName
	SampleBioMarkerTestData bioMarkerTestData = new SampleBioMarkerTestData()
	DeGplInfo platform
	List<PatientDimension> patients
	List<DeSubjectSampleMapping> assays
	List<DeQpcrMirnaAnnotation> probes
	List<DeSubjectMirnaData> mirnaData
	ConceptTestData concept

	MirnaTestData() {
		generateTestData()
	}

	MirnaTestData(String typeName) {
		this.typeName = typeName
		generateTestData()
	}

	protected void generateTestData() {
		concept = HighDimTestData.createConcept('MIRNAPUBLIC', 'concept code #1', TRIAL_NAME, 'MIRNA_CONCEPT')

		platform = new DeGplInfo(
				title: 'TaqMan® Rodent MicroRNA Array v3.0 A/B',
				organism: 'Mus musculus',
				markerType: typeName == 'mirnaseq' ? 'MIRNA_SEQ' : 'MIRNA_QPCR')
		platform.id = 'BOGUSGPL15466'

		patients = HighDimTestData.createTestPatients(2, -300, TRIAL_NAME)
		assays = HighDimTestData.createTestAssays(patients, -400, platform, TRIAL_NAME)

		probes = [createAnnotation(-501, 'hsa-mir-3161', 'mmu-miR-3161-4395373'),
		          createAnnotation(-502, null, 'snoRNA135-4380912'),
		          createAnnotation(-503, 'hsa-mir-323b', 'mmu-miR-323b-4373305')]

		double intensity = 0
		mirnaData = []
		for (probe in probes) {
			for (DeSubjectSampleMapping assay in assays) {
				mirnaData << createMirnaEntry(assay, probe, intensity += 0.1)
			}
		}
	}

	private DeQpcrMirnaAnnotation createAnnotation(long probesetId, String mirna, String detector) {
		DeQpcrMirnaAnnotation res = new DeQpcrMirnaAnnotation(
				mirnaId: mirna,
				detector: detector,
				platform: platform,
				gplId: 'gplId')
		res.id = probesetId
		res
	}

	private DeSubjectMirnaData createMirnaEntry(DeSubjectSampleMapping assay, DeQpcrMirnaAnnotation probe,
	                                            double intensity) {
		new DeSubjectMirnaData(
				probe: probe,
				jProbe: probe,
				assay: assay,
				patient: assay.patient,
				trialName: TRIAL_NAME,
				rawIntensity: intensity,
				logIntensity: Math.log(intensity) / Math.log(2),
				zscore: intensity * 2) // non-sensical value
	}

	void saveAll() {
		bioMarkerTestData.saveMirnaData()

		save platform, logger
		saveAll patients, logger
		saveAll assays, logger
		saveAll probes, logger
		saveAll mirnaData, logger

		concept.saveAll()
	}
}
