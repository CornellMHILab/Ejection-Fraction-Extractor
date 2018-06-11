package gov.va.vinci.ef.ae;

/*
 * #%L
 * Echo concept exctractor
 * %%
 * Copyright (C) 2010 - 2016 Department of Veterans Affairs
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import gov.va.vinci.ef.types.*;
import gov.va.vinci.leo.AnnotationLibrarian;
import gov.va.vinci.leo.ae.LeoBaseAnnotator;
import gov.va.vinci.leo.descriptors.LeoConfigurationParameter;
import gov.va.vinci.leo.descriptors.LeoTypeSystemDescription;
import gov.va.vinci.leo.tools.LeoUtils;
import gov.va.vinci.leo.window.WindowService;
import gov.va.vinci.leo.window.types.Window;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeDescription;
import org.apache.uima.resource.metadata.impl.TypeDescription_impl;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Flatten relationships for easier output.
 * <p>
 * Created by vhaslcpatteo on 9/16/2015. Edited by Thomas Ginter on 09/18/2015.
 * Added the setValueStrings method. Prakash added several methods.
 */

public class FlattenRelationAE extends LeoBaseAnnotator {

	/**
	 * Path to the regex file to parse.
	 */
	@LeoConfigurationParameter

	protected String regexFilePath = null;

	/**
	 * Patterns for which if there is a match in the window before the anchor
	 * then the relationship is invalid.
	 */
	protected Pattern[] efPatterns = null;
	protected Pattern[] rfPatterns = null;
	
	protected Pattern numberPattern = Pattern.compile("\\d+(\\.\\d+)?");
	protected Pattern highPattern = Pattern.compile("severe(ly)?\\s+depressed");
	protected Pattern moderatePattern = Pattern.compile("moderate(ly)?\\s+depressed");
	protected Pattern lowPattern = Pattern.compile("mild(ly)?\\s+depressed");
	protected Pattern closePattern = Pattern.compile("gross(ly)?\\s+preserved");
	protected Pattern normalPattern = Pattern.compile("appear(s)?\\s+depressed");

	/**
	   * Window service class.
	   */
	  protected WindowService windowService = new WindowService(2, 2, Window.class.getCanonicalName());
	
	
	/**
	 * Pattern flags for each regex.
	 */
	public static int PATTERN_FLAGS = Pattern.CASE_INSENSITIVE | Pattern.DOTALL;

	/**
	 * Log messages
	 */
	private static final Logger log = Logger.getLogger(LeoUtils.getRuntimeClass());

	@Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		super.initialize(aContext);

		if (StringUtils.isBlank(regexFilePath))
			throw new ResourceInitializationException("regexFilePath cannot be blank or null", null);

		try {
			parseRegexFile(new File(regexFilePath));
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	public void annotate(JCas aJCas) throws AnalysisEngineProcessException {
		
		// get today's date
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		
		Collection<EfRelation> relations = AnnotationLibrarian.getAllAnnotationsOfType(aJCas, EfRelation.type, false);

		if (relations.size() > 0) {
			for (EfRelation relation : relations) {
				// Create the output annotation
				Relation out = new Relation(aJCas, relation.getBegin(), relation.getEnd());
				
				// set run date
				out.setRunDate(timeStamp);
				
				out.addToIndexes();
				// Set the string value features
				setValueStrings(relation, out);
			}
		} else {
			// Prakash. Look for concept mapping between qualitative assessment
			// System.out.println("No relation exist: ");

			Collection<MeasurementWindow> windows = AnnotationLibrarian.getAllAnnotationsOfType(aJCas,
					MeasurementWindow.type, false);
			
//			Collection<gov.va.vinci.ef.types.ContextWindow> windows = AnnotationLibrarian.getAllAnnotationsOfType(aJCas,
//					ContextWindow.type, false);
			
			for (MeasurementWindow a : windows) {
				
				String covered = a.getCoveredText();
				
//				final String NEW_LINE = System.getProperty("line.separator");
//				String str = String.format(covered, NEW_LINE);
//				System.out.println(str);
				
				//check for any rf function match, if yes got to next
				if (hasRfMatch(rfPatterns, covered)) {
					// System.out.println("RF Cover:");
					continue;
				}
				
				// test for ef function match, if exist create a relation annotation.
				if (hasEfMatch(efPatterns, covered)) {

					covered = covered.replace(System.getProperty("line.separator"), "");
					
					// System.out.println("LF Covered: " + covered);
					boolean foundValid = false;
					boolean hasString = false;

					// map severely depressed case
					if (foundValid == false) {
						Matcher m = highPattern.matcher(covered);
						hasString = false;
						while (m.find()) {
							String header = covered.substring(m.start(), m.end());
							if (!GenericValidator.isBlankOrNull(header)) {
								hasString = true;
								foundValid = true;
							}
							if ((hasString == true) && (foundValid == true)) {
								// Create annotation
								Relation out = new Relation(aJCas, m.start(), m.end());
								out.setValue("5");
								out.setValue2("29");
								out.setTerm("LVEF");
								out.setValueString(header);
								out.setSnippetString(covered);
								out.addToIndexes();
							}
						}
					}

					// map moderately depressed case
					if (foundValid == false) {
						Matcher m = moderatePattern.matcher(covered);
						hasString = false;
						while (m.find()) {
							String header = covered.substring(m.start(), m.end());
							if (!GenericValidator.isBlankOrNull(header)) {
								hasString = true;
								foundValid = true;
							}
							if ((hasString == true) && (foundValid == true)) {
								// Create annotation
								Relation out = new Relation(aJCas, m.start(), m.end());
								out.setValue("30");
								out.setValue2("44");
								out.setTerm("LVEF");
								out.setValueString(header);
								out.setSnippetString(covered);
								out.addToIndexes();
							}
						}
					}

					// map mildly depressed case
					if (foundValid == false) {
						Matcher m = lowPattern.matcher(covered);
						hasString = false;
						while (m.find()) {
							String header = covered.substring(m.start(), m.end());
							if (!GenericValidator.isBlankOrNull(header)) {
								hasString = true;
								foundValid = true;
							}
							if ((hasString == true) && (foundValid == true)) {
								// Create annotation
								Relation out = new Relation(aJCas, m.start(), m.end());
								out.setValue("45");
								out.setValue2("54");
								out.setTerm("LVEF");
								out.setValueString(header);
								out.setSnippetString(covered);
								out.addToIndexes();
							}
						}
					}
					
					// map grossely preserved case
					if (foundValid == false) {
						Matcher m = closePattern.matcher(covered);
						hasString = false;
						while (m.find()) {
							String header = covered.substring(m.start(), m.end());
							if (!GenericValidator.isBlankOrNull(header)) {
								hasString = true;
								foundValid = true;
							}
							if ((hasString == true) && (foundValid == true)) {
								// Create annotation
								Relation out = new Relation(aJCas, m.start(), m.end());
								out.setValue("50");
								out.setValue2("55");
								out.setTerm("LVEF");
								out.setValueString(header);
								out.setSnippetString(covered);
								out.addToIndexes();
							}
						}
					}

					// normal depressed case
					if (foundValid == false) {
						Matcher m = normalPattern.matcher(covered);
						hasString = false;
						while (m.find()) {
							String header = covered.substring(m.start(), m.end());
							if (!GenericValidator.isBlankOrNull(header)) {
								hasString = true;
								foundValid = true;
							}
							if ((hasString == true) && (foundValid == true)) {
								// Create annotation
								Relation out = new Relation(aJCas, m.start(), m.end());
								out.setValue("55");
								out.setValue2("70");
								out.setTerm("LVEF");
								out.setValueString(header);
								out.setSnippetString(covered);
								out.addToIndexes();
							}
						}
					}
				}
			}
		}
	}

	protected void setValueStrings(EfRelation in, Relation out) {

		// Get the NumericValue annotation from the merged set
		Annotation value = null;
		Annotation measure = null;
		FSArray merged = in.getLinked();
		for (int i = 0; i < merged.size(); i++) {
			Annotation a = (Annotation) merged.get(i);
			String typeName = a.getType().getName();
			if (typeName.equals(Measurement.class.getCanonicalName())) {
				measure = a;
				if (measure != null) {
					out.setTerm(measure.getCoveredText());
				}
			}

			else if (typeName.equals(NumericValue.class.getCanonicalName())) {
				value = a;
			}
		}
		// Exit if no value found
		if (value == null)
			return;
		// Get the values
		String valueText = value.getCoveredText();

		// System.out.println("Value: " + value.getCoveredText());

		Matcher m = numberPattern.matcher(valueText);
		ArrayList<Double> values = new ArrayList<Double>(2);
		while (m.find())
			values.add(new Double(valueText.substring(m.start(), m.end())));
		Collections.sort(values);

		// Set the values
		if (values.size() > 0)
			out.setValue(values.get(0).toString());
		if (values.size() > 1)
			out.setValue2(values.get(values.size() - 1).toString());
		if (StringUtils.isNotBlank(valueText))
			out.setValueString(valueText);

	}

	/**
	 * Returns true if the text provided has a match in one of the patterns.
	 *
	 * @param patterns
	 * @param text
	 * @return
	 */
	protected boolean hasEfMatch(Pattern[] patterns, String text) {
		boolean hasMatch = false;
		for (Pattern p : patterns) {
			if (p.matcher(text).find()) {
				hasMatch = true;
				break;
			}
		}
		return hasMatch;
	}
	
	
	/**
	 * Returns true if the text provided has a match in one of the patterns.
	 *
	 * @param patterns
	 * @param text
	 * @return
	 */
	protected boolean hasRfMatch(Pattern[] patterns, String text) {
		boolean hasMatch = false;
		for (Pattern p : patterns) {
			if (p.matcher(text).find()) {
				hasMatch = true;
				break;
			}
		}
		return hasMatch;
	}
	

	/**
	 * Get the patterns from the regex file and stash them in the appropriate
	 * lists.
	 *
	 * @param regexFile
	 *            File from which to retrieve the patterns
	 * @throws IOException
	 *             If there is an error reading the file.
	 */
	protected void parseRegexFile(File regexFile) throws IOException {
		if (regexFile == null)
			throw new IllegalArgumentException("regexFile cannot be null");
		// List of Patterns to compile
		ArrayList<Pattern> efList = new ArrayList<Pattern>();
		ArrayList<Pattern> rfList = new ArrayList<Pattern>();
		
		int patternType = 3;		
		// Read in the lines of the regex file
		List<String> lines = IOUtils.readLines(FileUtils.openInputStream(regexFile));
		for (String line : lines) {
		      if (line.startsWith("#")) {
		        if (line.startsWith("#EFMAP"))
		          patternType = 1;
		        else if (line.startsWith("#RFMAP"))
		          patternType = 2;
		      } else if (StringUtils.isNotBlank(line)) {
		        if (patternType == 1)
		          efList.add(Pattern.compile(line, PATTERN_FLAGS));
		        else if (patternType == 2)
		          rfList.add(Pattern.compile(line, PATTERN_FLAGS));			        
		      }
		}
		// Stash each collection
		efPatterns = efList.toArray(new Pattern[efList.size()]);
		rfPatterns = rfList.toArray(new Pattern[rfList.size()]);

	}

	@Override
	public LeoTypeSystemDescription getLeoTypeSystemDescription() {
		TypeDescription relationFtsd;
		String relationParent = "gov.va.vinci.ef.types.Relation";
		relationFtsd = new TypeDescription_impl(relationParent, "", "uima.tcas.Annotation");
		
		relationFtsd.addFeature("RunDate", "", "uima.cas.String");
		
		relationFtsd.addFeature("Term", "", "uima.cas.String"); // Extracted
																// term string
		relationFtsd.addFeature("Value", "", "uima.cas.String"); // Numeric
		
		// Numeric value
		relationFtsd.addFeature("Value2", "", "uima.cas.String");
		
		// String value with units and extra modifiers
		relationFtsd.addFeature("ValueString", "", "uima.cas.String");

		// Snippet value with units and extra modifiers
		relationFtsd.addFeature("SnippetString", "", "uima.cas.String");
		
		LeoTypeSystemDescription types = new LeoTypeSystemDescription();
		try {
			types.addType(relationFtsd); // for target concepts with single
											// mapping

		} catch (Exception e) {
			e.printStackTrace();
		}
		return types;
	}

	public String getRegexFilePath() {
		return regexFilePath;
	}

	public FlattenRelationAE setRegexFilePath(String regexFilePath) {
		this.regexFilePath = regexFilePath;
		return this;
	}

}
