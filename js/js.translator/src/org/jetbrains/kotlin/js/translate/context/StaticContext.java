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

package org.jetbrains.kotlin.js.translate.context;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.dart.compiler.backend.js.ast.*;
import com.google.dart.compiler.backend.js.ast.metadata.HasMetadata;
import com.google.dart.compiler.backend.js.ast.metadata.MetadataProperties;
import com.google.dart.compiler.backend.js.ast.metadata.SideEffectKind;
import com.intellij.openapi.util.Factory;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.hash.LinkedHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.descriptors.*;
import org.jetbrains.kotlin.descriptors.impl.LocalVariableDescriptor;
import org.jetbrains.kotlin.js.config.JsConfig;
import org.jetbrains.kotlin.js.config.LibrarySourcesConfig;
import org.jetbrains.kotlin.js.translate.context.generator.Generator;
import org.jetbrains.kotlin.js.translate.context.generator.Rule;
import org.jetbrains.kotlin.js.translate.intrinsic.Intrinsics;
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils;
import org.jetbrains.kotlin.js.translate.utils.ManglingUtils;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.resolve.BindingContext;
import org.jetbrains.kotlin.resolve.BindingTrace;
import org.jetbrains.kotlin.resolve.DescriptorUtils;
import org.jetbrains.kotlin.resolve.calls.util.FakeCallableDescriptorForObject;
import org.jetbrains.kotlin.resolve.descriptorUtil.DescriptorUtilsKt;

import java.util.*;

import static org.jetbrains.kotlin.js.translate.utils.AnnotationsUtils.*;
import static org.jetbrains.kotlin.js.translate.utils.JsAstUtils.pureFqn;
import static org.jetbrains.kotlin.js.translate.utils.JsDescriptorUtils.*;
import static org.jetbrains.kotlin.js.translate.utils.ManglingUtils.getMangledName;
import static org.jetbrains.kotlin.js.translate.utils.ManglingUtils.getSuggestedName;
import static org.jetbrains.kotlin.resolve.DescriptorUtils.*;
import static org.jetbrains.kotlin.resolve.calls.tasks.DynamicCallsKt.isDynamic;

/**
 * Aggregates all the static parts of the context.
 */
public final class StaticContext {

    public static StaticContext generateStaticContext(@NotNull BindingTrace bindingTrace, @NotNull JsConfig config, @NotNull ModuleDescriptor moduleDescriptor) {
        JsProgram program = new JsProgram("main");
        Namer namer = Namer.newInstance(program.getRootScope());
        Intrinsics intrinsics = new Intrinsics();
        StandardClasses standardClasses = StandardClasses.bindImplementations(namer.getKotlinScope());
        JsFunction rootFunction = JsAstUtils.createFunctionWithEmptyBody(program.getScope());
        return new StaticContext(program, rootFunction, bindingTrace, namer, intrinsics, standardClasses, program.getRootScope(), config,
                                 moduleDescriptor);
    }

    @NotNull
    private final JsProgram program;

    @NotNull
    private final BindingTrace bindingTrace;
    @NotNull
    private final Namer namer;

    @NotNull
    private final Intrinsics intrinsics;

    @NotNull
    private final StandardClasses standardClasses;

    @NotNull
    private final JsScope rootScope;

    @NotNull
    private final Generator<JsName> names = new NameGenerator();
    @NotNull
    private final Generator<JsName> innerNames = new InnerNameGenerator();
    @NotNull
    private final Map<FqName, JsName> packageNames = Maps.newHashMap();
    @NotNull
    private final Generator<JsScope> scopes = new ScopeGenerator();
    @NotNull
    private final Generator<JsExpression> qualifiers = new QualifierGenerator();
    @NotNull
    private final Generator<Boolean> qualifierIsNull = new QualifierIsNullGenerator();
    @NotNull
    private final Generator<JsName> objectInstanceNames = new ObjectInstanceNameGenerator();

    @NotNull
    private final Map<JsScope, JsFunction> scopeToFunction = Maps.newHashMap();

    @NotNull
    private final Map<MemberDescriptor, List<DeclarationDescriptor>> classOrConstructorClosure = Maps.newHashMap();

    @NotNull
    private final Map<ClassDescriptor, List<DeferredCallSite>> deferredCallSites = new HashMap<ClassDescriptor, List<DeferredCallSite>>();

    @NotNull
    private final JsConfig config;

    @NotNull
    private final Map<String, JsName> importedModules = new LinkedHashMap<String, JsName>();

    private Map<String, JsName> readOnlyImportedModules;

    @NotNull
    private final ModuleDescriptor currentModule;

    @NotNull
    private JsFunction rootFunction;

    @NotNull
    private final List<JsStatement> declarationStatements = new ArrayList<JsStatement>();

    @NotNull
    private final List<JsStatement> importStatements = new ArrayList<JsStatement>();

    @NotNull
    private final List<JsStatement> exportStatements = new ArrayList<JsStatement>();

    @NotNull
    private final Set<ClassDescriptor> classes = new HashSet<ClassDescriptor>();

    @NotNull
    private final ExportedPackage rootPackage = new ExportedPackage("");

    @NotNull
    private final JsObjectLiteral exportObject = rootPackage.objectLiteral;

    //TODO: too many parameters in constructor
    private StaticContext(
            @NotNull JsProgram program,
            @NotNull JsFunction rootFunction,
            @NotNull BindingTrace bindingTrace,
            @NotNull Namer namer,
            @NotNull Intrinsics intrinsics,
            @NotNull StandardClasses standardClasses,
            @NotNull JsScope rootScope,
            @NotNull JsConfig config,
            @NotNull ModuleDescriptor moduleDescriptor
    ) {
        this.program = program;
        this.rootFunction = rootFunction;
        this.bindingTrace = bindingTrace;
        this.namer = namer;
        this.intrinsics = intrinsics;
        this.rootScope = rootScope;
        this.standardClasses = standardClasses;
        this.config = config;
        this.currentModule = moduleDescriptor;
        this.rootFunction = rootFunction;
    }

    @NotNull
    public JsProgram getProgram() {
        return program;
    }

    @NotNull
    public BindingTrace getBindingTrace() {
        return bindingTrace;
    }

    @NotNull
    public BindingContext getBindingContext() {
        return bindingTrace.getBindingContext();
    }

    @NotNull
    public Intrinsics getIntrinsics() {
        return intrinsics;
    }

    @NotNull
    public Namer getNamer() {
        return namer;
    }

    @NotNull
    private JsScope getRootScope() {
        return rootScope;
    }

    @NotNull
    public Map<String, JsName> getImportedModules() {
        if (readOnlyImportedModules == null) {
            readOnlyImportedModules = Collections.unmodifiableMap(importedModules);
        }
        return readOnlyImportedModules;
    }

    @NotNull
    public JsScope getScopeForDescriptor(@NotNull DeclarationDescriptor descriptor) {
        JsScope scope = scopes.get(descriptor.getOriginal());
        assert scope != null : "Must have a scope for descriptor";
        return scope;
    }

    @NotNull
    public JsFunction getFunctionWithScope(@NotNull CallableDescriptor descriptor) {
        JsScope scope = getScopeForDescriptor(descriptor);
        JsFunction function = scopeToFunction.get(scope);
        function.setName(rootFunction.getScope().declareFreshName(ManglingUtils.getSuggestedName(descriptor)));
        assert scope.equals(function.getScope()) : "Inconsistency.";
        return function;
    }

    @NotNull
    public JsNameRef getQualifiedReference(@NotNull DeclarationDescriptor descriptor) {
        if (descriptor instanceof PackageViewDescriptor) {
            return getQualifiedReference(((PackageViewDescriptor) descriptor).getFqName());
        }
        if (descriptor instanceof PackageFragmentDescriptor) {
            return getQualifiedReference(((PackageFragmentDescriptor) descriptor).getFqName());
        }

        JsNameRef result = new JsNameRef(getNameForDescriptor(descriptor), getQualifierForDescriptor(descriptor));
        applySideEffects(result, descriptor);
        return result;
    }

    @NotNull
    public JsNameRef getQualifiedReference(@NotNull FqName packageFqName) {
        JsName packageName = getNameForPackage(packageFqName);
        return pureFqn(packageName, packageFqName.isRoot() ? null : getQualifierForParentPackage(packageFqName.parent()));
    }

    @NotNull
    public JsName getNameForDescriptor(@NotNull DeclarationDescriptor descriptor) {
        JsName name = names.get(descriptor.getOriginal());
        assert name != null : "Must have name for descriptor";
        return name;
    }

    @NotNull
    public JsName getInnerNameForDescriptor(@NotNull DeclarationDescriptor descriptor) {
        JsName name = innerNames.get(descriptor.getOriginal());
        assert name != null : "Must have inner name for descriptor";
        return name;
    }

    @NotNull
    public JsName getNameForObjectInstance(@NotNull ClassDescriptor descriptor) {
        JsName name = objectInstanceNames.get(descriptor.getOriginal());
        assert name != null : "Must have inner name for object instance";
        return name;
    }

    @NotNull
    public JsName getNameForPackage(@NotNull final FqName packageFqName) {
        return ContainerUtil.getOrCreate(packageNames, packageFqName, new Factory<JsName>() {
            @Override
            public JsName create() {
                String name = Namer.generatePackageName(packageFqName);
                return getRootScope().declareName(name);
            }
        });
    }

    @NotNull
    private JsNameRef getQualifierForParentPackage(@NotNull FqName packageFqName) {
        JsNameRef result = null;
        JsNameRef qualifier = null;

        FqName fqName = packageFqName;

        while (true) {
            JsNameRef ref = pureFqn(getNameForPackage(fqName), null);

            if (qualifier == null) {
                result = ref;
            }
            else {
                qualifier.setQualifier(ref);
            }

            qualifier = ref;

            if (fqName.isRoot()) break;
            fqName = fqName.parent();
        }

        return result;
    }

    @NotNull
    public JsConfig getConfig() {
        return config;
    }

    private final class InnerNameGenerator extends Generator<JsName> {
        public InnerNameGenerator() {
            addRule(new Rule<JsName>() {
                @Nullable
                @Override
                public JsName apply(@NotNull DeclarationDescriptor descriptor) {
                    String suggestedName;
                    if (descriptor instanceof LocalVariableDescriptor || descriptor instanceof ParameterDescriptor) {
                        return getNameForDescriptor(descriptor);
                    }
                    if (descriptor instanceof PropertyGetterDescriptor) {
                        PropertyGetterDescriptor getter = (PropertyGetterDescriptor) descriptor;
                        suggestedName = "get_" + ManglingUtils.getSuggestedName(getter.getCorrespondingProperty());
                    }
                    else if (descriptor instanceof PropertySetterDescriptor) {
                        PropertySetterDescriptor setter = (PropertySetterDescriptor) descriptor;
                        suggestedName = "set_" + ManglingUtils.getSuggestedName(setter.getCorrespondingProperty());
                    }
                    else if (descriptor instanceof ConstructorDescriptor) {
                        if (((ConstructorDescriptor) descriptor).isPrimary()) {
                            return getInnerNameForDescriptor(((ConstructorDescriptor) descriptor).getConstructedClass());
                        }
                        suggestedName =ManglingUtils.getSuggestedName(descriptor) + "_init";
                    }
                    else {
                        suggestedName = ManglingUtils.getSuggestedName(descriptor);
                    }
                    JsName result = rootFunction.getScope().declareFreshName(suggestedName);
                    ModuleDescriptor module = DescriptorUtilsKt.getModule(descriptor);
                    if (module != currentModule) {
                        importStatements.add(JsAstUtils.newVar(result, getQualifiedReference(descriptor)));
                    }
                    return result;
                }
            });
        }
    }

    private final class ObjectInstanceNameGenerator extends Generator<JsName> {
        public ObjectInstanceNameGenerator() {
            addRule(new Rule<JsName>() {
                @Nullable
                @Override
                public JsName apply(@NotNull DeclarationDescriptor descriptor) {
                    String suggested = ManglingUtils.getSuggestedName(descriptor) + Namer.OBJECT_INSTANCE_FUNCTION_SUFFIX;
                    JsName result = rootFunction.getScope().declareFreshName(suggested);
                    ModuleDescriptor module = DescriptorUtilsKt.getModule(descriptor);
                    if (module != currentModule) {
                        importStatements.add(JsAstUtils.newVar(result, getQualifiedReference(descriptor)));
                    }
                    return result;
                }
            });
        }
    }

    private final class NameGenerator extends Generator<JsName> {

        public NameGenerator() {
            Rule<JsName> namesForDynamic = new Rule<JsName>() {
                @Override
                @Nullable
                public JsName apply(@NotNull DeclarationDescriptor descriptor) {
                    if (isDynamic(descriptor)) {
                        String name = descriptor.getName().asString();
                        return JsDynamicScope.INSTANCE.declareName(name);
                    }

                    return null;
                }
            };

            Rule<JsName> localClasses = new Rule<JsName>() {
                @Nullable
                @Override
                public JsName apply(@NotNull DeclarationDescriptor descriptor) {
                    if (!DescriptorUtils.isDescriptorWithLocalVisibility(descriptor) ||
                        !DescriptorUtils.isClass(descriptor)) {
                        return null;
                    }

                    String suggested = getSuggestedName(descriptor);

                    descriptor = getParentOfType(descriptor, ClassOrPackageFragmentDescriptor.class, true);
                    assert descriptor != null;

                    JsScope scope = getScopeForDescriptor(descriptor);
                    return scope.declareFreshName(suggested);
                }
            };

            Rule<JsName> namesForStandardClasses = new Rule<JsName>() {
                @Override
                @Nullable
                public JsName apply(@NotNull DeclarationDescriptor data) {
                    if (!standardClasses.isStandardObject(data)) {
                        return null;
                    }
                    return standardClasses.getStandardObjectName(data);
                }
            };
            Rule<JsName> memberDeclarationsInsideParentsScope = new Rule<JsName>() {
                @Override
                @Nullable
                public JsName apply(@NotNull DeclarationDescriptor descriptor) {
                    JsScope scope = getEnclosingScope(descriptor);
                    return scope.declareFreshName(getSuggestedName(descriptor));
                }
            };
            Rule<JsName> constructorOrNativeCompanionObjectHasTheSameNameAsTheClass = new Rule<JsName>() {
                @Override
                public JsName apply(@NotNull DeclarationDescriptor descriptor) {
                    if (descriptor instanceof ConstructorDescriptor && ((ConstructorDescriptor) descriptor).isPrimary() ||
                        DescriptorUtils.isCompanionObject(descriptor) && isNativeObject(descriptor)) {
                        //noinspection ConstantConditions
                        return getNameForDescriptor(descriptor.getContainingDeclaration());
                    }
                    return null;
                }
            };

            // ecma 5 property name never declares as obfuscatable:
            // 1) property cannot be overloaded, so, name collision is not possible
            // 2) main reason: if property doesn't have any custom accessor, value holder will have the same name as accessor, so, the same name will be declared more than once
            //
            // But extension property may obfuscatable, because transform into function. Example: String.foo = 1, Int.foo = 2
            Rule<JsName> propertyOrPropertyAccessor = new Rule<JsName>() {
                @Override
                public JsName apply(@NotNull DeclarationDescriptor descriptor) {
                    PropertyDescriptor propertyDescriptor;
                    if (descriptor instanceof PropertyAccessorDescriptor) {
                        propertyDescriptor = ((PropertyAccessorDescriptor) descriptor).getCorrespondingProperty();
                    }
                    else if (descriptor instanceof PropertyDescriptor) {
                        propertyDescriptor = (PropertyDescriptor) descriptor;
                    }
                    else {
                        return null;
                    }

                    String nameFromAnnotation = getNameForAnnotatedObjectWithOverrides(propertyDescriptor);
                    if (nameFromAnnotation != null) {
                        return declarePropertyOrPropertyAccessorName(descriptor, nameFromAnnotation, false);
                    }

                    String propertyName = getSuggestedName(propertyDescriptor);

                    if (!isExtension(propertyDescriptor)) {
                        if (Visibilities.isPrivate(propertyDescriptor.getVisibility())) {
                            propertyName = getMangledName(propertyDescriptor, propertyName);
                        }
                        return declarePropertyOrPropertyAccessorName(descriptor, propertyName, false);
                    } else {
                        assert !(descriptor instanceof PropertyDescriptor) : "descriptor should not be instance of PropertyDescriptor: " + descriptor;

                        boolean isGetter = descriptor instanceof PropertyGetterDescriptor;
                        String accessorName = Namer.getNameForAccessor(propertyName, isGetter, false);
                        return declarePropertyOrPropertyAccessorName(descriptor, accessorName, false);
                    }
                }
            };

            Rule<JsName> predefinedObjectsHasUnobfuscatableNames = new Rule<JsName>() {
                @Override
                public JsName apply(@NotNull DeclarationDescriptor descriptor) {
                    // The mixing of override and rename by annotation(e.g. native) is forbidden.
                    if (descriptor instanceof CallableMemberDescriptor &&
                        !((CallableMemberDescriptor) descriptor).getOverriddenDescriptors().isEmpty()) {
                        return null;
                    }

                    if (descriptor instanceof ConstructorDescriptor) {
                        DeclarationDescriptor classDescriptor = descriptor.getContainingDeclaration();
                        assert classDescriptor != null;
                        descriptor = classDescriptor;
                    }

                    String name = getNameForAnnotatedObjectWithOverrides(descriptor);
                    if (name != null) return getEnclosingScope(descriptor).declareName(name);
                    return null;
                }
            };

            Rule<JsName> overridingDescriptorsReferToOriginalName = new Rule<JsName>() {
                @Override
                public JsName apply(@NotNull DeclarationDescriptor descriptor) {
                    //TODO: refactor
                    if (!(descriptor instanceof FunctionDescriptor)) {
                        return null;
                    }
                    FunctionDescriptor overriddenDescriptor = getOverriddenDescriptor((FunctionDescriptor) descriptor);
                    if (overriddenDescriptor == null) {
                        return null;
                    }

                    JsScope scope = getEnclosingScope(descriptor);
                    JsName result = getNameForDescriptor(overriddenDescriptor);
                    scope.declareName(result.getIdent());
                    return result;
                }
            };

            Rule<JsName> fakeCallableDescriptor = new Rule<JsName>() {
                @Nullable
                @Override
                public JsName apply(@NotNull DeclarationDescriptor descriptor) {
                    if (!(descriptor instanceof FakeCallableDescriptorForObject)) {
                        return null;
                    }

                    FakeCallableDescriptorForObject fakeCallableDescriptor = (FakeCallableDescriptorForObject) descriptor;
                    return getNameForDescriptor(fakeCallableDescriptor.getReferencedObject());
                }
            };

            addRule(namesForDynamic);
            addRule(localClasses);
            addRule(namesForStandardClasses);
            addRule(constructorOrNativeCompanionObjectHasTheSameNameAsTheClass);
            addRule(propertyOrPropertyAccessor);
            addRule(predefinedObjectsHasUnobfuscatableNames);
            addRule(overridingDescriptorsReferToOriginalName);
            addRule(fakeCallableDescriptor);
            addRule(memberDeclarationsInsideParentsScope);
        }
    }

    @NotNull
    public JsName declarePropertyOrPropertyAccessorName(@NotNull DeclarationDescriptor descriptor, @NotNull String name, boolean fresh) {
        JsScope scope = getEnclosingScope(descriptor);
        return fresh ? scope.declareFreshName(name) : scope.declareName(name);
    }

    @NotNull
    private JsScope getEnclosingScope(@NotNull DeclarationDescriptor descriptor) {
        DeclarationDescriptor containingDeclaration = getContainingDeclaration(descriptor);
        return getScopeForDescriptor(containingDeclaration.getOriginal());
    }

    private final class ScopeGenerator extends Generator<JsScope> {

        public ScopeGenerator() {
            Rule<JsScope> generateNewScopesForClassesWithNoAncestors = new Rule<JsScope>() {
                @Override
                public JsScope apply(@NotNull DeclarationDescriptor descriptor) {
                    if (!(descriptor instanceof ClassDescriptor)) {
                        return null;
                    }
                    if (getSuperclass((ClassDescriptor) descriptor) == null) {
                        return getRootScope().innerObjectScope("Scope for class " + descriptor.getName());
                    }
                    return null;
                }
            };
            Rule<JsScope> generateInnerScopesForDerivedClasses = new Rule<JsScope>() {
                @Override
                public JsScope apply(@NotNull DeclarationDescriptor descriptor) {
                    if (!(descriptor instanceof ClassDescriptor)) {
                        return null;
                    }
                    ClassDescriptor superclass = getSuperclass((ClassDescriptor) descriptor);
                    if (superclass == null) {
                        return null;
                    }
                    return getScopeForDescriptor(superclass).innerObjectScope("Scope for class " + descriptor.getName());
                }
            };
            Rule<JsScope> generateNewScopesForPackageDescriptors = new Rule<JsScope>() {
                @Override
                public JsScope apply(@NotNull DeclarationDescriptor descriptor) {
                    if (!(descriptor instanceof PackageFragmentDescriptor)) {
                        return null;
                    }
                    return getRootScope().innerObjectScope("Package " + descriptor.getName());
                }
            };
            //TODO: never get there
            Rule<JsScope> generateInnerScopesForMembers = new Rule<JsScope>() {
                @Override
                public JsScope apply(@NotNull DeclarationDescriptor descriptor) {
                    JsScope enclosingScope = getEnclosingScope(descriptor);
                    return enclosingScope.innerObjectScope("Scope for member " + descriptor.getName());
                }
            };
            Rule<JsScope> createFunctionObjectsForCallableDescriptors = new Rule<JsScope>() {
                @Override
                public JsScope apply(@NotNull DeclarationDescriptor descriptor) {
                    if (!(descriptor instanceof CallableDescriptor)) {
                        return null;
                    }
                    JsScope enclosingScope = getEnclosingScope(descriptor);

                    JsFunction correspondingFunction = JsAstUtils.createFunctionWithEmptyBody(enclosingScope);
                    assert (!scopeToFunction.containsKey(correspondingFunction.getScope())) : "Scope to function value overridden for " + descriptor;
                    scopeToFunction.put(correspondingFunction.getScope(), correspondingFunction);
                    return correspondingFunction.getScope();
                }
            };
            addRule(createFunctionObjectsForCallableDescriptors);
            addRule(generateNewScopesForClassesWithNoAncestors);
            addRule(generateInnerScopesForDerivedClasses);
            addRule(generateNewScopesForPackageDescriptors);
            addRule(generateInnerScopesForMembers);
        }
    }

    @Nullable
    public JsExpression getQualifierForDescriptor(@NotNull DeclarationDescriptor descriptor) {
        if (qualifierIsNull.get(descriptor.getOriginal()) != null) {
            return null;
        }
        return qualifiers.get(descriptor.getOriginal());
    }

    private final class QualifierGenerator extends Generator<JsExpression> {
        public QualifierGenerator() {
            Rule<JsExpression> standardObjectsHaveKotlinQualifier = new Rule<JsExpression>() {
                @Override
                public JsExpression apply(@NotNull DeclarationDescriptor descriptor) {
                    if (!standardClasses.isStandardObject(descriptor)) {
                        return null;
                    }
                    return Namer.kotlinObject();
                }
            };
            //TODO: review and refactor
            Rule<JsExpression> packageLevelDeclarationsHaveEnclosingPackagesNamesAsQualifier = new Rule<JsExpression>() {
                @Override
                public JsExpression apply(@NotNull DeclarationDescriptor descriptor) {
                    if (isNativeObject(descriptor)) return null;

                    DeclarationDescriptor containingDescriptor = getContainingDeclaration(descriptor);
                    if (!(containingDescriptor instanceof PackageFragmentDescriptor)) {
                        return null;
                    }

                    JsNameRef result = getQualifierForParentPackage(((PackageFragmentDescriptor) containingDescriptor).getFqName());

                    JsExpression moduleExpression = getModuleExpressionFor(descriptor);
                    return moduleExpression != null ? JsAstUtils.replaceRootReference(result, moduleExpression) : result;
                }
            };
            Rule<JsExpression> constructorOrCompanionObjectHasTheSameQualifierAsTheClass = new Rule<JsExpression>() {
                @Override
                public JsExpression apply(@NotNull DeclarationDescriptor descriptor) {
                    if (descriptor instanceof ConstructorDescriptor ||
                        isNativeObject(descriptor) && DescriptorUtils.isCompanionObject(descriptor)) {
                        //noinspection ConstantConditions
                        return getQualifierForDescriptor(descriptor.getContainingDeclaration());
                    }
                    return null;
                }
            };
            Rule<JsExpression> libraryObjectsHaveKotlinQualifier = new Rule<JsExpression>() {
                @Override
                public JsExpression apply(@NotNull DeclarationDescriptor descriptor) {
                    if (isLibraryObject(descriptor)) {
                        return Namer.kotlinObject();
                    }
                    return null;
                }
            };
            Rule<JsExpression> nativeObjectsHaveNativePartOfFullQualifier = new Rule<JsExpression>() {
                @Override
                public JsExpression apply(@NotNull DeclarationDescriptor descriptor) {
                    if (descriptor instanceof ConstructorDescriptor || !isNativeObject(descriptor)) return null;

                    DeclarationDescriptor containingDeclaration = descriptor.getContainingDeclaration();
                    if (containingDeclaration != null && isNativeObject(containingDeclaration)) {
                        return isCompanionObject(descriptor) ? getQualifierForDescriptor(containingDeclaration) :
                            getQualifiedReference(containingDeclaration);
                    }

                    return null;
                }
            };
            Rule<JsExpression> staticMembersHaveContainerQualifier = new Rule<JsExpression>() {
                @Override
                public JsExpression apply(@NotNull DeclarationDescriptor descriptor) {
                    if (descriptor instanceof CallableDescriptor && !isNativeObject(descriptor)) {
                        CallableDescriptor callableDescriptor = (CallableDescriptor) descriptor;
                        if (DescriptorUtils.isStaticDeclaration(callableDescriptor)) {
                            return getQualifiedReference(callableDescriptor.getContainingDeclaration());
                        }
                    }

                    return null;
                }
            };
            Rule<JsExpression> nestedClassesHaveContainerQualifier = new Rule<JsExpression>() {
                @Nullable
                @Override
                public JsExpression apply(@NotNull DeclarationDescriptor descriptor) {
                    if (!(descriptor instanceof ClassDescriptor)) {
                        return null;
                    }
                    DeclarationDescriptor container = getParentOfType(descriptor, ClassDescriptor.class);
                    if (container == null) {
                        return null;
                    }

                    if (isNativeObject(descriptor)) {
                        return null;
                    }
                    return getQualifiedReference(container);
                }
            };

            Rule<JsExpression> localClassesHavePackageQualifier = new Rule<JsExpression>() {
                @Nullable
                @Override
                public JsExpression apply(@NotNull DeclarationDescriptor descriptor) {
                    if (!DescriptorUtils.isDescriptorWithLocalVisibility(descriptor) || !(descriptor instanceof ClassDescriptor)) {
                        return null;
                    }

                    descriptor = getParentOfType(descriptor, PackageFragmentDescriptor.class, true);
                    assert descriptor != null;
                    return getQualifiedReference(descriptor);
                }
            };

            addRule(libraryObjectsHaveKotlinQualifier);
            addRule(constructorOrCompanionObjectHasTheSameQualifierAsTheClass);
            addRule(standardObjectsHaveKotlinQualifier);
            addRule(packageLevelDeclarationsHaveEnclosingPackagesNamesAsQualifier);
            addRule(nativeObjectsHaveNativePartOfFullQualifier);
            addRule(staticMembersHaveContainerQualifier);
            addRule(nestedClassesHaveContainerQualifier);
            addRule(localClassesHavePackageQualifier);
        }
    }

    @Nullable
    public JsExpression getModuleExpressionFor(@NotNull DeclarationDescriptor descriptor) {
        String moduleName = getExternalModuleName(descriptor);
        if (moduleName == null) return null;

        if (LibrarySourcesConfig.UNKNOWN_EXTERNAL_MODULE_NAME.equals(moduleName)) return null;

        JsName moduleId = moduleName.equals(Namer.KOTLIN_LOWER_NAME) ? rootScope.declareName(Namer.KOTLIN_NAME) :
                          importedModules.get(moduleName);
        if (moduleId == null) {
            moduleId = rootScope.declareFreshName(Namer.LOCAL_MODULE_PREFIX + Namer.suggestedModuleName(moduleName));
            importedModules.put(moduleName, moduleId);
        }

        return JsAstUtils.pureFqn(moduleId, null);
    }

    private static JsExpression applySideEffects(JsExpression expression, DeclarationDescriptor descriptor) {
        if (expression instanceof HasMetadata) {
            if (descriptor instanceof FunctionDescriptor ||
                descriptor instanceof PackageFragmentDescriptor ||
                descriptor instanceof ClassDescriptor
            ) {
                MetadataProperties.setSideEffects((HasMetadata) expression, SideEffectKind.PURE);
            }
        }
        return expression;
    }

    private static class QualifierIsNullGenerator extends Generator<Boolean> {

        private QualifierIsNullGenerator() {
            Rule<Boolean> propertiesInClassHaveNoQualifiers = new Rule<Boolean>() {
                @Override
                public Boolean apply(@NotNull DeclarationDescriptor descriptor) {
                    if ((descriptor instanceof PropertyDescriptor) && descriptor.getContainingDeclaration() instanceof ClassDescriptor) {
                        return true;
                    }
                    return null;
                }
            };
            addRule(propertiesInClassHaveNoQualifiers);
        }
    }

    public void putClassOrConstructorClosure(@NotNull MemberDescriptor localClass, @NotNull List<DeclarationDescriptor> closure) {
        classOrConstructorClosure.put(localClass, Lists.newArrayList(closure));
    }

    @Nullable
    public List<DeclarationDescriptor> getClassOrConstructorClosure(@NotNull MemberDescriptor descriptor) {
        List<DeclarationDescriptor> result = classOrConstructorClosure.get(descriptor);
        return result != null ? Lists.newArrayList(result) : null;
    }

    @NotNull
    public Map<ClassDescriptor, List<DeferredCallSite>> getDeferredCallSites() {
        return deferredCallSites;
    }

    @NotNull
    public JsFunction getRootFunction() {
        return rootFunction;
    }

    @NotNull
    public List<JsStatement> getDeclarationStatements() {
        return declarationStatements;
    }

    public void addRootStatement(@NotNull JsStatement statement) {
        declarationStatements.add(statement);
    }

    public void addClass(@NotNull ClassDescriptor classDescriptor) {
        classes.add(classDescriptor);
    }

    public void export(@NotNull DeclarationDescriptor descriptor) {
        DeclarationDescriptor container = descriptor.getContainingDeclaration();
        if (!(container instanceof PackageFragmentDescriptor)) {
            throw new IllegalArgumentException("Declaration " + descriptor + " is not a top-level declaration");
        }

        PackageFragmentDescriptor packageDescriptor = (PackageFragmentDescriptor) container;
        ExportedPackage exportedPackage = rootPackage;
        for (Name packageName : packageDescriptor.getFqName().pathSegments()) {
            exportedPackage = exportedPackage.getSubpackage(packageName.asString());
        }

        JsExpression initializerExpr = DeclarationExporter.exportDeclaration(this, descriptor, exportStatements);
        if (initializerExpr != null) {
            JsPropertyInitializer initializer = new JsPropertyInitializer(
                    getNameForDescriptor(descriptor).makeRef(), initializerExpr);
            exportedPackage.objectLiteral.getPropertyInitializers().add(initializer);
        }
    }

    @NotNull
    public ModuleDescriptor getCurrentModule() {
        return currentModule;
    }

    public void postProcess() {
        rootFunction.getBody().getStatements().addAll(importStatements);
        addClassPrototypes();
        rootFunction.getBody().getStatements().addAll(declarationStatements);

        JsName rootPackageName = rootFunction.getScope().declareName(Namer.getRootPackageName());
        rootFunction.getBody().getStatements().add(JsAstUtils.newVar(rootPackageName, exportObject));
        rootFunction.getBody().getStatements().addAll(exportStatements);
    }

    private void addClassPrototypes() {
        Set<ClassDescriptor> visited = new HashSet<ClassDescriptor>();
        for (ClassDescriptor cls : classes) {
            addClassPrototypes(cls, visited);
        }
    }

    private void addClassPrototypes(@NotNull ClassDescriptor cls, @NotNull Set<ClassDescriptor> visited) {
        if (DescriptorUtilsKt.getModule(cls) != currentModule) return;
        if (!visited.add(cls)) return;

        ClassDescriptor superclass = DescriptorUtilsKt.getSuperClassNotAny(cls);
        if (superclass != null) {
            addClassPrototypes(superclass, visited);

            List<JsStatement> statements = rootFunction.getBody().getStatements();

            JsExpression superPrototype = JsAstUtils.prototypeOf(new JsNameRef(getInnerNameForDescriptor(superclass)));
            JsExpression superPrototypeInstance = new JsInvocation(new JsNameRef("create", "Object"), superPrototype);
            JsExpression classRef = new JsNameRef(getInnerNameForDescriptor(cls));
            JsExpression prototype = JsAstUtils.prototypeOf(classRef);
            statements.add(JsAstUtils.assignment(prototype, superPrototypeInstance).makeStmt());

            JsExpression constructorRef = new JsNameRef("constructor", prototype.deepCopy());
            statements.add(JsAstUtils.assignment(constructorRef, classRef.deepCopy()).makeStmt());
        }
    }

    private static class ExportedPackage {
        @NotNull final String name;
        @NotNull final Map<String, ExportedPackage> subpackages = new HashMap<String, ExportedPackage>();
        @NotNull final JsObjectLiteral objectLiteral = new JsObjectLiteral(true);

        public ExportedPackage(@NotNull String name) {
            this.name = name;
        }

        @NotNull
        public ExportedPackage getSubpackage(@NotNull String name) {
            ExportedPackage subpackage = subpackages.get(name);
            if (subpackage == null) {
                subpackage = new ExportedPackage(name);
                subpackages.put(name, subpackage);
                objectLiteral.getPropertyInitializers().add(new JsPropertyInitializer(new JsNameRef(name), subpackage.objectLiteral));
            }
            return subpackage;
        }
    }
}
