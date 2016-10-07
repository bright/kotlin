/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.idea.codeInsight;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.test.KotlinTestUtils;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.TestsPackage}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@RunWith(JUnit3RunnerWithInners.class)
public class InspectionTestGenerated extends AbstractInspectionTest {
    @TestMetadata("idea/testData/intentions")
    @TestDataPath("$PROJECT_ROOT")
    @RunWith(JUnit3RunnerWithInners.class)
    public static class Intentions extends AbstractInspectionTest {
        public void testAllFilesPresentInIntentions() throws Exception {
            KotlinTestUtils.assertAllTestsPresentInSingleGeneratedClass(this.getClass(), new File("idea/testData/intentions"), Pattern.compile("^(inspections\\.test)$"));
        }

        @TestMetadata("branched/ifThenToElvis/inspectionData/inspections.test")
        public void testBranched_ifThenToElvis_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/intentions/branched/ifThenToElvis/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("branched/ifThenToSafeAccess/inspectionData/inspections.test")
        public void testBranched_ifThenToSafeAccess_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/intentions/branched/ifThenToSafeAccess/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("conventionNameCalls/replaceGetOrSet/inspectionData/inspections.test")
        public void testConventionNameCalls_replaceGetOrSet_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/intentions/conventionNameCalls/replaceGetOrSet/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("convertToStringTemplate/inspectionData/inspections.test")
        public void testConvertToStringTemplate_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/intentions/convertToStringTemplate/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("deprecatedCallableAddReplaceWith/inspectionData/inspections.test")
        public void testDeprecatedCallableAddReplaceWith_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/intentions/deprecatedCallableAddReplaceWith/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("destructuringInLambda/inspectionData/inspections.test")
        public void testDestructuringInLambda_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/intentions/destructuringInLambda/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("iterationOverMap/inspectionData/inspections.test")
        public void testIterationOverMap_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/intentions/iterationOverMap/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("objectLiteralToLambda/inspectionData/inspections.test")
        public void testObjectLiteralToLambda_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/intentions/objectLiteralToLambda/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("removeExplicitSuperQualifier/inspectionData/inspections.test")
        public void testRemoveExplicitSuperQualifier_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/intentions/removeExplicitSuperQualifier/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("removeExplicitTypeArguments/inspectionData/inspections.test")
        public void testRemoveExplicitTypeArguments_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/intentions/removeExplicitTypeArguments/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("simplifyNegatedBinaryExpression/inspectionData/inspections.test")
        public void testSimplifyNegatedBinaryExpression_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/intentions/simplifyNegatedBinaryExpression/inspectionData/inspections.test");
            doTest(fileName);
        }
    }

    @TestMetadata("idea/testData/inspections")
    @TestDataPath("$PROJECT_ROOT")
    @RunWith(JUnit3RunnerWithInners.class)
    public static class Inspections extends AbstractInspectionTest {
        @TestMetadata("addVarianceModifier/inspectionData/inspections.test")
        public void testAddVarianceModifier_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/addVarianceModifier/inspectionData/inspections.test");
            doTest(fileName);
        }

        public void testAllFilesPresentInInspections() throws Exception {
            KotlinTestUtils.assertAllTestsPresentInSingleGeneratedClass(this.getClass(), new File("idea/testData/inspections"), Pattern.compile("^(inspections\\.test)$"));
        }

        @TestMetadata("androidIllegalIdentifiers/inspectionData/inspections.test")
        public void testAndroidIllegalIdentifiers_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/androidIllegalIdentifiers/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("arrayInDataClass/inspectionData/inspections.test")
        public void testArrayInDataClass_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/arrayInDataClass/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("canBeParameter/inspectionData/inspections.test")
        public void testCanBeParameter_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/canBeParameter/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("canBePrimaryConstructorProperty/inspectionData/inspections.test")
        public void testCanBePrimaryConstructorProperty_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/canBePrimaryConstructorProperty/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("canBeVal/inspectionData/inspections.test")
        public void testCanBeVal_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/canBeVal/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("conflictingExtensionProperty/inspectionData/inspections.test")
        public void testConflictingExtensionProperty_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/conflictingExtensionProperty/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("convertLambdaToReference/inspectionData/inspections.test")
        public void testConvertLambdaToReference_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/convertLambdaToReference/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("convertSecondaryToPrimary/inspectionData/inspections.test")
        public void testConvertSecondaryToPrimary_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/convertSecondaryToPrimary/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("equalsAndHashCode/inspectionData/inspections.test")
        public void testEqualsAndHashCode_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/equalsAndHashCode/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("gradleWrongPluginVersion/inspectionData/inspections.test")
        public void testGradleWrongPluginVersion_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/gradleWrongPluginVersion/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("hasPlatformType/inspectionData/inspections.test")
        public void testHasPlatformType_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/hasPlatformType/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("leakingThis/inspectionData/inspections.test")
        public void testLeakingThis_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/leakingThis/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("overridingDeprecatedMember/inspectionData/inspections.test")
        public void testOverridingDeprecatedMember_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/overridingDeprecatedMember/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("protectedInFinal/inspectionData/inspections.test")
        public void testProtectedInFinal_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/protectedInFinal/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("redundantIf/inspectionData/inspections.test")
        public void testRedundantIf_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/redundantIf/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("redundantModalityModifier/inspectionData/inspections.test")
        public void testRedundantModalityModifier_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/redundantModalityModifier/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("redundantSamConstructor/inspectionData/inspections.test")
        public void testRedundantSamConstructor_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/redundantSamConstructor/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("redundantSemicolon/inspectionData/inspections.test")
        public void testRedundantSemicolon_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/redundantSemicolon/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("redundantVisibilityModifier/inspectionData/inspections.test")
        public void testRedundantVisibilityModifier_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/redundantVisibilityModifier/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("removeSetterParameterType/inspectionData/inspections.test")
        public void testRemoveSetterParameterType_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/removeSetterParameterType/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("removeSingleExpressionStringTemplate/inspectionData/inspections.test")
        public void testRemoveSingleExpressionStringTemplate_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/removeSingleExpressionStringTemplate/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("removeToStringInStringTemplate/inspectionData/inspections.test")
        public void testRemoveToStringInStringTemplate_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/removeToStringInStringTemplate/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("replaceArrayEqualityOpWithArraysEquals/inspectionData/inspections.test")
        public void testReplaceArrayEqualityOpWithArraysEquals_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/replaceArrayEqualityOpWithArraysEquals/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("replaceCallWithComparison/inspectionData/inspections.test")
        public void testReplaceCallWithComparison_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/replaceCallWithComparison/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("spelling/inspectionData/inspections.test")
        public void testSpelling_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/spelling/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("twoSetOfTypeparameters/inspectionData/inspections.test")
        public void testTwoSetOfTypeparameters_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/twoSetOfTypeparameters/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("unusedImport/inspectionData/inspections.test")
        public void testUnusedImport_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/unusedImport/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("unusedReceiverParameter/inspectionData/inspections.test")
        public void testUnusedReceiverParameter_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/unusedReceiverParameter/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("unusedSymbol/class/inspectionData/inspections.test")
        public void testUnusedSymbol_class_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/unusedSymbol/class/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("unusedSymbol/enum/inspectionData/inspections.test")
        public void testUnusedSymbol_enum_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/unusedSymbol/enum/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("unusedSymbol/function/inspectionData/inspections.test")
        public void testUnusedSymbol_function_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/unusedSymbol/function/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("unusedSymbol/js/inspectionData/inspections.test")
        public void testUnusedSymbol_js_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/unusedSymbol/js/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("unusedSymbol/object/inspectionData/inspections.test")
        public void testUnusedSymbol_object_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/unusedSymbol/object/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("unusedSymbol/parameter/inspectionData/inspections.test")
        public void testUnusedSymbol_parameter_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/unusedSymbol/parameter/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("unusedSymbol/property/inspectionData/inspections.test")
        public void testUnusedSymbol_property_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/unusedSymbol/property/inspectionData/inspections.test");
            doTest(fileName);
        }

        @TestMetadata("unusedSymbol/typeParameter/inspectionData/inspections.test")
        public void testUnusedSymbol_typeParameter_inspectionData_Inspections_test() throws Exception {
            String fileName = KotlinTestUtils.navigationMetadata("idea/testData/inspections/unusedSymbol/typeParameter/inspectionData/inspections.test");
            doTest(fileName);
        }
    }
}
