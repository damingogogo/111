# Question Bank Management Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a backend-admin question bank page that supports manual question creation, local intelligent question generation, and creating emotional tests from selected bank questions.

**Architecture:** Reuse existing `screenings` as emotional tests and `screening_questions` as the question bank. Add focused Spring endpoints for question-bank workflows that are awkward in the generic CRUD page, and add one dedicated Vue view for test creation, manual authoring, intelligent generation, selection, preview, and batch save.

**Tech Stack:** Spring Boot 3, JDBC, MySQL/Flyway, Vue 3, Element Plus, Vite.

---

### Task 1: Backend Question Generation

**Files:**
- Create: `backend/src/main/java/com/danzi/emotion/service/QuestionGeneratorService.java`
- Test: `backend/src/test/java/com/danzi/emotion/QuestionGeneratorServiceTest.java`

- [ ] **Step 1: Write failing tests**

Create tests that assert generated questions use the requested topic, requested count, single-choice answer type, Likert options with scores, and varied dimensions.

- [ ] **Step 2: Run red test**

Run: `D:\devtools\apache-maven-3.9.15\bin\mvn.cmd -Dtest=QuestionGeneratorServiceTest test`
Expected: compilation fails because `QuestionGeneratorService` does not exist.

- [ ] **Step 3: Implement generator**

Create a deterministic local generator. It should not call an external AI service. It should combine theme templates, dimension labels, and five-level options into question maps that can be reviewed and saved by the admin page.

- [ ] **Step 4: Run green test**

Run: `D:\devtools\apache-maven-3.9.15\bin\mvn.cmd -Dtest=QuestionGeneratorServiceTest test`
Expected: all tests pass.

### Task 2: Backend Question Bank API

**Files:**
- Create: `backend/src/main/java/com/danzi/emotion/controller/QuestionBankController.java`
- Modify: `backend/src/main/java/com/danzi/emotion/service/TableRegistry.java`
- Test: `backend/src/test/java/com/danzi/emotion/TableRegistryTest.java`

- [ ] **Step 1: Update failing registry expectation**

Update `TableRegistryTest` to expect the existing table count if changed and confirm `screening_questions` exposes fields needed by the new page.

- [ ] **Step 2: Add API endpoints**

Add endpoints under `/api/question-bank`:
- `GET /screenings`
- `POST /screenings`
- `GET /questions`
- `POST /questions`
- `POST /questions/batch`
- `POST /generate`

- [ ] **Step 3: Implement validation and inserts**

Use `JdbcTemplate`. Store options as JSON text. Default status to `启用`, answer type to `single`, score rule to `likert_5`, and image URL to the local/proxy theme fallback when blank.

- [ ] **Step 4: Run backend tests**

Run: `D:\devtools\apache-maven-3.9.15\bin\mvn.cmd test`
Expected: all backend tests pass.

### Task 3: Admin UI Question Bank Page

**Files:**
- Create: `admin-web/src/views/QuestionBankView.vue`
- Modify: `admin-web/src/router.js`
- Modify: `admin-web/src/App.vue`

- [ ] **Step 1: Add route and menu**

Add `/question-bank` route and left-menu entry named `题库管理`, keeping the current blue-purple light UI style.

- [ ] **Step 2: Build page sections**

Add four panels:
- `创建情感测试`
- `手动出题`
- `智能出题`
- `题库列表/选择组卷`

- [ ] **Step 3: Wire API calls**

Use existing `api` helper. Generated questions should appear in a preview list and can be edited before batch saving.

- [ ] **Step 4: Build admin**

Run: `npm run build` in `admin-web`.
Expected: Vite build exits 0.

### Task 4: Full Verification

**Files:**
- Verify backend and admin build outputs.

- [ ] **Step 1: Backend package**

Run: `D:\devtools\apache-maven-3.9.15\bin\mvn.cmd -DskipTests package`
Expected: exit 0.

- [ ] **Step 2: Admin build**

Run: `npm run build` in `admin-web`.
Expected: exit 0.

- [ ] **Step 3: Mobile build**

Run: `npm run build:mp-weixin` in `mobile-uniapp`.
Expected: exit 0, because new tests created in the backend should still be usable by the existing mini-program AI screening page.
